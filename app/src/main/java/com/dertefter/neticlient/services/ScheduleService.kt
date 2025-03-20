package com.dertefter.neticlient.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.repository.ScheduleRepository
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.Time
import com.dertefter.neticlient.data.repository.SettingsRepository
import com.dertefter.neticlient.data.repository.UserRepository
import com.dertefter.neticlient.common.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.Calendar
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleService : Service() {

    @Inject
    lateinit var scheduleRepository: ScheduleRepository

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var userRepository: UserRepository

    lateinit var job: Job

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()

        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                checkCurrentLesson()
                checkFutureLesson()
                delay(20_000)
            }
        }

        createNotificationChannels()
        startForeground(NOTIFICATION_SERVICE_ID, buildServiceNotification())
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        job.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
        hideFutureLessonNotification()
        hideLessonNotification()
    }

    private suspend fun checkCurrentLesson() {
        val group = getCurrentGroup() ?: return
        val weekNumber = scheduleRepository.fetchCurrentWeekNumber() ?: return
        val schedule = scheduleRepository.getLocalSchedule(group).first() ?: return
        val dayNumber = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let {
            when (it) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> -1
            }
        }

        if (dayNumber == -1) {
            hideLessonNotification()
            return
        }

        val filteredTimeList = Utils.getFilteredTimeList(schedule, weekNumber, dayNumber)
        var foundActiveLesson = false

        for (timeItem in filteredTimeList) {
            val timeStart = timeItem.getTimeStart()
            val timeEnd = timeItem.getTimeEnd()
            if (timeStart.isBefore(LocalTime.now()) && timeEnd.isAfter(LocalTime.now())) {
                showLessonNotification(timeItem.lessons.first(), timeItem)
                foundActiveLesson = true
                break
            }
        }

        if (!foundActiveLesson) {
            hideLessonNotification()
        }
    }

    private suspend fun checkFutureLesson() {
        val enabledFutureLessons = settingsRepository.getNotifyFutureLessons().first()
        Log.e("eeeeee", enabledFutureLessons.toString())
        if (!enabledFutureLessons) {
            hideFutureLessonNotification()
            return
        }
        val group = getCurrentGroup() ?: return
        if (group.isEmpty()){
            hideFutureLessonNotification()
            return
        }

        val weekNumber = scheduleRepository.fetchCurrentWeekNumber() ?: return
        val schedule = scheduleRepository.getLocalSchedule(group).first() ?: return
        val dayNumber = Calendar.getInstance().get(Calendar.DAY_OF_WEEK).let {
            when (it) {
                Calendar.MONDAY -> 1
                Calendar.TUESDAY -> 2
                Calendar.WEDNESDAY -> 3
                Calendar.THURSDAY -> 4
                Calendar.FRIDAY -> 5
                Calendar.SATURDAY -> 6
                else -> -1
            }
        }

        if (dayNumber == -1) {
            hideFutureLessonNotification()
            return
        }

        val filteredTimeList = Utils.getFilteredTimeList(schedule, weekNumber, dayNumber)
        val currentTime = LocalTime.now()
        val futureTime = currentTime.plusMinutes(15)

        var upcomingLesson: Lesson? = null
        var upcomingTime: Time? = null
        var minStart: LocalTime? = null

        for (timeItem in filteredTimeList) {
            val timeStart = timeItem.getTimeStart()
            if (timeStart.isAfter(currentTime) && !timeStart.isAfter(futureTime)) {
                if (minStart == null || timeStart.isBefore(minStart)) {
                    minStart = timeStart
                    upcomingLesson = timeItem.lessons.first()
                    upcomingTime = timeItem
                }
            }
        }

        if (upcomingLesson != null && upcomingTime != null) {
            val minutesUntilStart = java.time.Duration.between(currentTime, minStart).toMinutes()
            showFutureLessonNotification(upcomingLesson, upcomingTime, minutesUntilStart)
        } else {
            hideFutureLessonNotification()
        }
    }



    private fun hideLessonNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_LESSON_ID)
    }

    private fun buildServiceNotification(): Notification {
        val intent = Intent(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
            putExtra(Settings.EXTRA_CHANNEL_ID, CHANNEL_SERVICE_ID)
        }
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(this, CHANNEL_SERVICE_ID)
            .setContentTitle("Сервис расписания работает")
            .setContentText("Нажмите здесь, чтобы скрыть это уведомление")
            .setSmallIcon(R.drawable.bolt_icon)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }



    private fun createNotificationChannels() {
        val serviceChannel = NotificationChannel(
            CHANNEL_SERVICE_ID,
            "Сервис расписания",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Фоновое уведомление о работе сервиса расписания" }

        val lessonChannel = NotificationChannel(
            CHANNEL_LESSON_ID,
            "Расписание занятий",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Уведомления о текущих занятиях" }

        val futureLessonChannel = NotificationChannel(
            CHANNEL_FUTURE_LESSON_ID,
            "Предстоящие занятия",
            NotificationManager.IMPORTANCE_LOW
        ).apply { description = "Уведомления о предстоящих занятиях" }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
        manager.createNotificationChannel(lessonChannel)
        manager.createNotificationChannel(futureLessonChannel)
    }

    private suspend fun getCurrentGroup(): String? {
        return userRepository.getSelectedGroup().first()
    }

    companion object {
        private const val CHANNEL_SERVICE_ID = "schedule_service_channel"
        private const val CHANNEL_LESSON_ID = "schedule_lesson_channel"
        private const val CHANNEL_FUTURE_LESSON_ID = "schedule_future_lesson_channel"
        private const val NOTIFICATION_SERVICE_ID = 1
        private const val NOTIFICATION_LESSON_ID = 2
        private const val NOTIFICATION_FUTURE_LESSON_ID = 3
    }

    private fun showLessonNotification(lesson: Lesson, time: Time) {
        val start = time.getTimeStart()
        val end = time.getTimeEnd()
        val totalDuration = end.toSecondOfDay() - start.toSecondOfDay()
        val currentProgress = LocalTime.now().toSecondOfDay() - start.toSecondOfDay()
        val progressPercentage = (currentProgress.toFloat() / totalDuration.toFloat()) * 100

        val notification = buildLessonNotification(
            lesson,
            progress = progressPercentage.toInt(),
            timeStart = time.timeStart,
            timeEnd = time.timeEnd
        )

        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_LESSON_ID, notification)
    }

    private fun showFutureLessonNotification(lesson: Lesson, time: Time, minutesUntilStart: Long) {
        val notification = buildFutureLessonNotification(lesson, time, minutesUntilStart)
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).notify(NOTIFICATION_FUTURE_LESSON_ID, notification)
    }

    private fun hideFutureLessonNotification() {
        (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_FUTURE_LESSON_ID)
    }

    private fun buildLessonNotification(lesson: Lesson, progress: Int, timeStart: String, timeEnd: String): Notification {
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_schedule_large)
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, lesson.title)
        notificationLayoutExpanded.setTextViewText(R.id.timeStart, timeStart)
        notificationLayoutExpanded.setTextViewText(R.id.aud, lesson.aud)
        notificationLayoutExpanded.setTextViewText(R.id.timeEnd, timeEnd)
        notificationLayoutExpanded.setProgressBar(R.id.progressBar, 100, progress, false)

        return NotificationCompat.Builder(this, CHANNEL_LESSON_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(notificationLayoutExpanded)
            .setSmallIcon(R.drawable.bolt_icon)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }

    private fun buildFutureLessonNotification(lesson: Lesson, time: Time, minutesUntilStart: Long): Notification {
        val notificationLayoutExpanded = RemoteViews(packageName, R.layout.notification_future_lesson_large)
        notificationLayoutExpanded.setTextViewText(R.id.notification_title, lesson.title)
        notificationLayoutExpanded.setTextViewText(R.id.aud, lesson.aud)
        val suffix = if (minutesUntilStart.toInt() in 2..4){
            "ы"
        } else if (minutesUntilStart.toInt() == 1){
            "у"
        }else {
            ""
        }
        if (minutesUntilStart < 1L){
            notificationLayoutExpanded.setTextViewText(R.id.`when`, "Скоро начнётся")
        }else {
            notificationLayoutExpanded.setTextViewText(R.id.`when`, "Начнётся через $minutesUntilStart минут$suffix")
        }


        return NotificationCompat.Builder(this, CHANNEL_FUTURE_LESSON_ID)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomBigContentView(notificationLayoutExpanded)
            .setSmallIcon(R.drawable.bolt_icon)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(false)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }


}
