package com.dertefter.neticlient.foreground

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.dertefter.neticlient.R
import com.dertefter.neticlient.ui.main.MainActivity
import com.dertefter.neticore.NETICore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@AndroidEntryPoint
class ScheduleService : Service() {

    @Inject lateinit var netiCore: NETICore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val channelId = "my_foreground_channel"
    private val notificationId = 100 - 7

    override fun onCreate() {
        super.onCreate()

        createNotificationChannel()

        val notification = buildNotification(
            getString(R.string.n_loading_schedule),
            getString(R.string.please_wait),
        )
        startForeground(notificationId, notification)

        serviceScope.launch {
            while (isActive) {
                try {
                    val currentDate = LocalDate.now()
                    val currentTime = LocalTime.now()

                    val group = netiCore.userDetailFeature.currentGroup.first()

                    var title = getString(R.string.app_name)
                    var body = getString(R.string.please_wait)
                    var aud: String = ""
                    var type: String = ""
                    var progress: Int? = null
                    var timeStartString: String = ""
                    var timeEndString: String = ""

                    if (group.isNullOrEmpty()){
                        title = getString(R.string.no_group)
                        body = getString(R.string.n_open_app_find_group)
                    } else{
                        val schedule = netiCore.scheduleFeature.scheduleForGroup(group).first()
                        if (schedule == null) {
                            netiCore.scheduleFeature.updateScheduleForGroup(group)
                            title = getString(R.string.n_loading_schedule)
                            body = getString(R.string.please_wait)
                        } else {
                            val nextDayWithLessons = schedule.findNextDayWithLessonsAfter(date = currentDate, time = currentTime)
                            val lesson = nextDayWithLessons?.findNextLesson(date = currentDate, localTime = currentTime)

                            if (lesson == null) {
                                title = getString(R.string.near_lessons)
                                body = getString(R.string.no_lessons_for_all)
                            } else {
                                val timeStart = lesson.getTimeStart()
                                val timeEnd = lesson.getTimeEnd()
                                val date = lesson.getLocalDate()
                                title = lesson.title
                                aud = lesson.aud
                                type = lesson.type
                                timeEndString = lesson.timeEnd
                                timeStartString = lesson.timeStart

                                when {
                                    currentDate.isBefore(date) -> {
                                        val dateString = getDateString(date)
                                        val p1 = getString(R.string.will_start)
                                        val p2 = dateString
                                        val p3 = getString(R.string.at)
                                        val p4 = timeStart
                                        body = "$p1 $p2$p3$p4"
                                    }
                                    currentDate.isAfter(date) -> {
                                        body = getString(R.string.lesson_over)
                                    }
                                    currentTime.isBefore(timeStart) -> {
                                        val p1 = getString(R.string.will_start)
                                        val p2 = getString(R.string.at)
                                        val p3 = timeStart
                                        body = "$p1$p2$p3"
                                    }
                                    currentTime.isAfter(timeEnd) -> {
                                        body = getString(R.string.lesson_over)
                                    }
                                    else -> {
                                        progress = calculateProgress(timeStart, timeEnd, currentTime)
                                        val p1 = getString(R.string.now)
                                        body = p1
                                    }
                                }


                            }
                        }
                    }


                    val notification = buildNotification(
                        title,
                        body,
                        aud,
                        type,
                        progress,
                        timeStartString,
                        timeEndString
                    )

                    updateNotification(notification)
                } catch (e: Exception) {
                    Log.e("widget error", e.stackTraceToString())
                    val notification = buildNotification(
                        title = getString(R.string.near_lessons),
                        body = getString(R.string.load_fail)
                    )
                    updateNotification(notification)
                }

                delay(6000L)
            }
        }
    }

    private fun buildNotification(title: String, body: String, aud: String = "", type: String = "", progress: Int? = null, timeStart: String = "", timeEnd: String = ""): Notification {
        val smallView = RemoteViews(packageName, R.layout.notification_lesson_small).apply {
            setTextViewText(R.id.notification_title, title)
        }

        val bigView = RemoteViews(packageName, R.layout.notification_lesson_large).apply {
            setTextViewText(R.id.notification_title, title)
            setTextViewText(R.id.notification_body, body)
            setTextViewText(R.id.timeStart, timeStart)
            setTextViewText(R.id.timeEnd, timeEnd)
            setTextViewText(R.id.type, type)
            setTextViewText(R.id.aud, aud)

            if (progress != null){
                setProgressBar(R.id.progress, 100, progress, false)
            }
            setViewVisibility(R.id.on_going_container, if (progress != null) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.aud, if (aud.isNotEmpty()) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.type, if (type.isNotEmpty()) View.VISIBLE else View.GONE)
        }

        // интент для клика
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setOngoing(true)
            .setPriority(2)
            .setShowWhen(false)
            .setContentIntent(pendingIntent)
            .setCustomContentView(smallView)      // кастом для обычного вида
            .setCustomBigContentView(bigView)     // кастом для развёрнутого вида
            .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // чтобы сохранились стандартные отступы
            .build()
    }


    private fun updateNotification(notification: Notification) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            channelId,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    fun getDateString(date: LocalDate): String {
        return when (date) {
            LocalDate.now() -> getString(R.string.today)
            LocalDate.now().plusDays(1) -> getString(R.string.tomorrow)
            LocalDate.now().minusDays(1) -> getString(R.string.yesterday)
            else -> {
                val formatter = DateTimeFormatter.ofPattern("d MMMM")
                date.format(formatter)
            }
        }
    }

    private fun calculateProgress(
        startTime: LocalTime?,
        endTime: LocalTime?,
        currentTime: LocalTime?
    ): Int {
        if (startTime == null || endTime == null || currentTime == null) return 0

        val totalMinutes = startTime.until(endTime, ChronoUnit.MINUTES)
        val elapsedMinutes = startTime.until(currentTime, ChronoUnit.MINUTES)

        return if (totalMinutes > 0) {
            ((elapsedMinutes.toDouble() / totalMinutes.toDouble()) * 100)
                .toInt().coerceIn(0, 100)
        } else 0
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
