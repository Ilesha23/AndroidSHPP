package com.iyakovlev.task4.presentation.adapters

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.iyakovlev.task4.presentation.fragments.ContactsFragment
import com.iyakovlev.task4.presentation.fragments.MainFragment
import com.iyakovlev.task4.utils.Constants.ARG_OBJECT
import java.lang.IllegalArgumentException

class ViewPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MainFragment()
            1 -> ContactsFragment()
            else ->throw IllegalArgumentException("Invalid position: $position")
        }
    }

}