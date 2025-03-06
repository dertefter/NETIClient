package com.dertefter.neticlient.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.palette.graphics.Palette
import com.dertefter.neticlient.R
import com.dertefter.neticlient.data.model.schedule.Lesson
import com.dertefter.neticlient.data.model.schedule.LessonTrigger
import com.dertefter.neticlient.data.model.schedule.NextTimeInfo
import com.dertefter.neticlient.data.model.schedule.Schedule
import com.dertefter.neticlient.data.model.schedule.Time
import java.util.Locale

object Utils {
    fun formatDayName(dateString: String): String {
        val locale = Locale("ru", "RU")
        val inputFormat = SimpleDateFormat("dd.MM.yy", locale)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("EEEE", locale)
        return outputFormat.format(date).toString()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }

    fun formatDate(dateString: String): String {
        val locale = Locale("ru", "RU")
        val inputFormat = SimpleDateFormat("dd.MM.yy", locale)
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("dd.MM", locale)
        return outputFormat.format(date)
    }

    fun dpToPx(context: Context, dp: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }

    fun basicAnimationOn(v: View): AnimatorSet {
        v.visibility = View.VISIBLE
        val alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 1f)
        val scaleXAnim = ObjectAnimator.ofFloat(v, "scaleX", 1f)
        val scaleYAnim = ObjectAnimator.ofFloat(v, "scaleY",  1f)

        return AnimatorSet().apply {
            playTogether(alphaAnim, scaleXAnim, scaleYAnim)
            duration = 300
            interpolator = OvershootInterpolator(1f)
        }
    }

    fun basicAnimationOff(v: View, needGone: Boolean = true): AnimatorSet {
        val alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 0f)
        val scaleXAnim = ObjectAnimator.ofFloat(v, "scaleX", 0.81f)
        val scaleYAnim = ObjectAnimator.ofFloat(v, "scaleY",  0.8f)
        return AnimatorSet().apply {
            playTogether(alphaAnim, scaleXAnim, scaleYAnim)
            duration = 150
            interpolator = OvershootInterpolator(1.5f)
            doOnEnd {
                if (needGone){
                    v.visibility = View.GONE
                }

            }
        }
    }

    fun basicTransitionAnimations(): NavOptions {
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.open)
            .setExitAnim(R.anim.close)
            .setPopEnterAnim(R.anim.open)
            .setPopExitAnim(R.anim.close)
            .build()
        return navOptions
    }

    fun NavController.navigateTo(destinationId: Int, args: Bundle? = null) {
        this.navigate(destinationId, args, navOptions = basicTransitionAnimations())
    }

    fun TextView.displayHtml(html: String) {
        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, this)

        // Using Html framework to parse html
        val styledText= HtmlCompat.fromHtml(html,
            HtmlCompat.FROM_HTML_MODE_LEGACY,
            imageGetter,null)

        // to enable image/link clicking
        this.movementMethod = LinkMovementMethod.getInstance()

        // setting the text after formatting html and downloading and setting images
        this.text = styledText
    }

    // Generate palette synchronously and return it.
    fun createPaletteSync(bitmap: Bitmap): Palette = Palette.from(bitmap).generate()

    // Generate palette asynchronously and use it on a different thread using onGenerated().
    fun createPaletteAsync(bitmap: Bitmap) {
        Palette.from(bitmap).generate { palette ->
            // Use generated instance.
        }
    }

    fun getFilteredTimeList(schedule: Schedule, weekNumber: Int, dayNumber: Int): List<Time> {
        try{
            val output = mutableListOf<Time>()
            val days = schedule.days
            if (days.isEmpty()) return emptyList()
            val day = days[dayNumber - 1]
            for (timeItem in day.times){
                val lessonItems = mutableListOf<Lesson>()
                val lessons = timeItem.lessons
                for (l in lessons){
                    if (
                        (l.trigger == LessonTrigger.ALL) ||
                        (l.trigger == LessonTrigger.ODD && weekNumber % 2 != 0) ||
                        (l.trigger == LessonTrigger.EVEN && weekNumber % 2 == 0) ||
                        (l.trigger == LessonTrigger.CUSTOM && l.triggerWeeks.contains(weekNumber))
                    ) {
                        lessonItems.add(l)
                    }
                }
                if (lessonItems.isNotEmpty()){
                    output.add(
                        Time(
                            timeItem.timeStart, timeItem.timeEnd, lessonItems
                        )
                    )
                }
            }
            return output
        }catch (e: Exception){
            Log.e("getFilteredTimeList", e.stackTraceToString())
            return emptyList()
        }
    }

    fun showToast(requireContext: Context, s: String) {
        Toast.makeText(requireContext, s, Toast.LENGTH_SHORT).show()

    }


}