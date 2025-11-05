package com.webpage.vevamos

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class RegisterViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val fragments: List<Fragment> = listOf(
        RegisterEmailFragment(),
        RegisterPasswordFragment(),
        RegisterNameFragment(),
        RegisterBirthDateFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}