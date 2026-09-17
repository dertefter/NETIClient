package com.dertefter.neticlient.widgets.near_schedule.presentation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.preview.ExperimentalGlancePreviewApi
import androidx.glance.preview.Preview
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.dertefter.data.dto.schedule.LessonDto
import com.dertefter.data.dto.schedule.TimeSlotDto
import com.dertefter.neticlient.MainActivity
import com.dertefter.neticlient.R
import com.dertefter.neticlient.widgets.near_schedule.presentation.components.TimeSlotItem
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun NearScheduleWidgetContent(groupName: String?, schedule: List<TimeSlotDto>?) {
    val context = LocalContext.current
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.widgetBackground)
            .padding(top = 12.dp)
            .padding(horizontal = 12.dp)
            .clickable(actionStartActivity(Intent(LocalContext.current, MainActivity::class.java)))
    ) {
        if (groupName == null) {
            Box(modifier = GlanceModifier
                .padding(8.dp)
                .fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = context.getString(R.string.app_select_group_in_app),
                    style = TextStyle(color = GlanceTheme.colors.onBackground, fontSize = 16.sp)
                )
            }
        } else {
            val nextDayData = getNextDayData(schedule)
            val date = nextDayData?.first
            val timeSlots = nextDayData?.second ?: emptyList()

            val nowDate = LocalDate.now()
            val diff: Long? = if (date == null) null else ChronoUnit.DAYS.between(nowDate, date)
            val primaryLine: String? = when (diff) {
                null -> context.getString(R.string.app_no_lessons_found)
                0L -> context.getString(R.string.app_pretty_date_today)
                1L -> context.getString(R.string.app_pretty_date_tomorrow)
                2L -> context.getString(R.string.app_pretty_date_after_tomorrow)
                in 3L..6L -> context.resources.getQuantityString(R.plurals.app_pretty_date_days, diff.toInt(), diff.toInt())
                else -> null
            }
            val locale = context.resources.configuration.locales[0]
            val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM", locale)
            val secondaryLine = date?.format(formatter)?.replaceFirstChar { it.uppercase() }

            val title = primaryLine ?: (secondaryLine ?: "")
            val subtitle: String? = if (title == secondaryLine) null else secondaryLine

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = groupName.uppercase(),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = GlanceTheme.colors.tertiary
                    )
                )

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = GlanceModifier.defaultWeight()
                ) {
                    Text(
                        text = title,
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GlanceTheme.colors.onBackground
                        )
                    )
                    subtitle?.let { subtitle ->
                        Text(
                            text = subtitle,
                            style = TextStyle(
                                fontSize = 12.sp,
                                color = GlanceTheme.colors.onBackground
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(8.dp))

            if (timeSlots.isEmpty()) {
                Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), contentAlignment = Alignment.Center) {
                    Text(text = context.getString(R.string.app_no_lessons), style = TextStyle(color = GlanceTheme.colors.onSurface))
                }
            } else {
                LazyColumn(modifier = GlanceModifier.fillMaxWidth()) {
                    items(timeSlots) { timeSlot ->
                        TimeSlotItem(timeSlot)
                    }
                    item{
                        Spacer(
                            modifier = GlanceModifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun getNextDayData(schedule: List<TimeSlotDto>?): Pair<LocalDate, List<TimeSlotDto>>? {
    val today = LocalDate.now()
    val nowTime = LocalTime.now()
    val grouped = schedule?.groupBy { it.getDate() } ?: return null

    val todayLessons = grouped[today]
    val targetDate =
        if (todayLessons != null && todayLessons.any { it.getEndTime().isAfter(nowTime) }) {
            today
        } else {
            grouped.keys.filter { it.isAfter(today) }.minOrNull()
        }

    return targetDate?.let { it to (grouped[it] ?: emptyList()) }
}

@Suppress("unused")
@OptIn(ExperimentalGlancePreviewApi::class)
@Composable
@Preview
fun ScheduleWidgetContentPreview() {
    val sampleTimeSlots = listOf(
        TimeSlotDto(
            dateString = LocalDate.now().plusDays(1).toString(),
            startTimeString = "08:30",
            endTimeString = "10:00",
            lessons = listOf(
                LessonDto(
                    name = "Высшая математика",
                    type = "Лекция",
                    aud = "301",
                    persons = null
                )
            )
        ),
        TimeSlotDto(
            dateString = LocalDate.now().plusDays(2).toString(),
            startTimeString = "10:10",
            endTimeString = "11:40",
            lessons = listOf(
                LessonDto(
                    name = "Физика",
                    type = "Практика",
                    aud = "202",
                    persons = null
                )
            )
        )
    )

    GlanceTheme {
        NearScheduleWidgetContent(
            groupName = "АБВ-123",
            schedule = sampleTimeSlots
        )
    }
}
