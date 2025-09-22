package com.dertefter.neticlient.common.utils

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import android.widget.Toast
import androidx.core.animation.doOnEnd
import androidx.core.text.HtmlCompat
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.palette.graphics.Palette
import com.dertefter.neticlient.R
import java.util.Locale
import androidx.core.text.parseAsHtml

object Utils {

    fun basicAnimationOn(v: View): AnimatorSet {
        v.visibility = View.VISIBLE
        val alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 1f)
        return AnimatorSet().apply {
            playTogether(alphaAnim)
            duration = 300
            interpolator = OvershootInterpolator(1f)
        }
    }

    fun basicAnimationOff(v: View, needGone: Boolean = true): AnimatorSet {
        val alphaAnim = ObjectAnimator.ofFloat(v, "alpha", 0f)
        return AnimatorSet().apply {
            playTogether(alphaAnim)
            duration = 150
            interpolator = OvershootInterpolator(1.5f)
            doOnEnd {
                if (needGone){
                    v.visibility = View.GONE
                }

            }
        }
    }

    fun getDefaultNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.animator.nav_default_enter_anim)
            .setExitAnim(R.animator.nav_default_exit_anim)
            .setPopEnterAnim(R.animator.nav_default_pop_enter_anim)
            .setPopExitAnim(R.animator.nav_default_pop_exit_anim)
            .build()
    }


    fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setEnterAnim(R.animator.nav_enter_anim)
            .setExitAnim(R.animator.nav_exit_anim)
            .setPopEnterAnim(R.animator.nav_pop_enter_anim)
            .setPopExitAnim(R.animator.nav_pop_exit_anim)
            .build()
    }


    fun NavController.navigateTo(destinationId: Int, args: Bundle? = null) {
        this.navigate(destinationId, args, navOptions = getNavOptions())
    }

    fun TextView.displayHtml(html: String) {
        // Creating object of ImageGetter class you just created
        val imageGetter = ImageGetter(resources, this)

        // Using Html framework to parse html
        val styledText= html.parseAsHtml(HtmlCompat.FROM_HTML_MODE_COMPACT, imageGetter)

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

    fun showToast(requireContext: Context, s: String) {
        Toast.makeText(requireContext, s, Toast.LENGTH_SHORT).show()
    }


}