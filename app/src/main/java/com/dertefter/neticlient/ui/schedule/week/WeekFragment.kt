package com.dertefter.neticlient.ui.schedule.week

import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.widget.ViewPager2
import com.dertefter.neticlient.R
import com.dertefter.neticlient.databinding.FragmentWeekBinding
import com.dertefter.neticlient.ui.schedule.ScheduleFragment
import com.dertefter.neticlient.ui.schedule.ScheduleViewModel
import com.dertefter.neticore.features.schedule.model.Week
import com.google.android.material.color.MaterialColors
import com.google.android.material.tabs.TabLayoutMediator
import java.time.format.DateTimeFormatter
import java.util.Locale

class WeekFragment : Fragment() {

    lateinit var binding: FragmentWeekBinding
    private var _adapter: DaysAdapter? = null

    private val adapter: DaysAdapter
        get() = _adapter ?: DaysAdapter(this).also {
            _adapter = it
        }

    var mediator: TabLayoutMediator? = null
    var pagerCallback: ViewPager2.OnPageChangeCallback? = null
    private val scheduleViewModel: ScheduleViewModel by activityViewModels()
    private var dayIndex = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _adapter = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val daysTabLayout = (parentFragment as ScheduleFragment).binding.daysTabLayout
        val yearAndMonth = (parentFragment as ScheduleFragment).binding.yearAndMounth

        binding.pager.adapter = adapter
        binding.pager.offscreenPageLimit = 6

        val week = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable("WEEK", Week::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable("WEEK") as? Week
        }

        if (week != null){
            Log.e("sdshjdsjhkcds", week.toString())
            adapter.updateData(week)
            val inflater = LayoutInflater.from(requireContext())
            mediator?.detach()
            mediator = TabLayoutMediator(daysTabLayout, binding.pager) { tab, position ->

                val day = week.days[position]
                val customTabView = inflater.inflate(R.layout.day_tab, null)

                val dateTextView = customTabView.findViewById<TextView>(R.id.dayDate)
                val dayNameTextView = customTabView.findViewById<TextView>(R.id.dayName)

                val formatter = DateTimeFormatter.ofPattern("E", Locale.getDefault())

                dateTextView.text = day.getDate()?.dayOfMonth.toString()
                dayNameTextView.text = day.getDate()?.format(formatter) ?: day.dayName

                tab.customView = customTabView
            }
            mediator?.attach()
            pagerCallback?.let { binding.pager.unregisterOnPageChangeCallback(it) }
            pagerCallback = object : ViewPager2.OnPageChangeCallback() {

                private fun animateSmth(view: View, translationY: Float, scale: Float, alpha: Float) {
                    val translationYAnim = ObjectAnimator.ofFloat(view, "translationY", translationY)
                    val scaleXAnim = ObjectAnimator.ofFloat(view, "scaleX", scale)
                    val scaleYAnim = ObjectAnimator.ofFloat(view, "scaleY", scale)
                    val alphaAnim = ObjectAnimator.ofFloat(view, "alpha", alpha)

                    val interpolator = 	PathInterpolator(0.2f, 0f, 0f, 1f)

                    translationYAnim.interpolator = interpolator
                    scaleXAnim.interpolator = interpolator
                    scaleYAnim.interpolator = interpolator
                    alphaAnim.interpolator = interpolator

                    translationYAnim.start()
                    scaleXAnim.start()
                    scaleYAnim.start()
                    alphaAnim.start()
                }



                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    if (position in week.days.indices) {
                        val date = week.days[position].getDate()
                        val formatter = DateTimeFormatter.ofPattern("LLLL, yyyy", Locale("ru"))
                        val formattedDate = date?.format(formatter)?.replaceFirstChar { it.uppercaseChar() }
                        yearAndMonth.text = formattedDate
                    }

                    for (i in 0 until daysTabLayout.tabCount) {
                        val tab = daysTabLayout.getTabAt(i)
                        val customView = tab?.customView
                        val isSelected = i == position
                        val textColorAttr = if (isSelected)
                            com.google.android.material.R.attr.colorOnPrimaryContainer
                        else
                            com.google.android.material.R.attr.colorOnSurface

                        val density = context?.resources?.displayMetrics?.density ?: 1f
                        val dayNameView = customView?.findViewById<TextView>(R.id.dayName)?: return
                        val dayDateView = customView?.findViewById<TextView>(R.id.dayDate)?: return
                        if (isSelected) {
                            animateSmth(dayNameView, -20 * density, 2f, 1f)
                            animateSmth(dayDateView, 24 * density, 3f, 0.2f)
                        } else {
                            animateSmth(dayNameView, 0f, 1f, 0.85f)
                            animateSmth(dayDateView, 0f, 1f, 1f)
                        }


                        customView?.findViewById<TextView>(R.id.dayDate)?.setTextColor(
                            MaterialColors.getColor(binding.root, textColorAttr)
                        )
                        customView?.findViewById<TextView>(R.id.dayName)?.setTextColor(
                            MaterialColors.getColor(binding.root, textColorAttr)
                        )
                    }
                }
            }
            binding.pager.registerOnPageChangeCallback(pagerCallback as ViewPager2.OnPageChangeCallback)

        }

    }

    override fun onDestroy() {
        super.onDestroy()
        pagerCallback?.let { binding.pager.unregisterOnPageChangeCallback(it) }
        pagerCallback = null
    }
}