package com.webpage.vevamos.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class VerificationPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    // Lista de los pasos de verificación
    val fragments: List<Fragment> = listOf(
        VerificationSelfieFragment(),
        VerificationIdFragment(),
        VerificationPreferencesFragment()
        // Aquí se añadiría el de antecedentes penales
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}