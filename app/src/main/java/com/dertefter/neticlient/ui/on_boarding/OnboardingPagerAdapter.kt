package com.dertefter.neticlient.ui.on_boarding

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.dertefter.neticlient.ui.on_boarding.pages.OnboardingPage1
import com.dertefter.neticlient.ui.on_boarding.pages.OnboardingPage2
import com.dertefter.neticlient.ui.on_boarding.pages.OnboardingPage3
import com.dertefter.neticlient.ui.on_boarding.pages.OnboardingPage4

class OnboardingPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    val fragments = listOf(
        OnboardingPage1(),
        OnboardingPage2(),
        OnboardingPage3(),
        OnboardingPage4(),
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}