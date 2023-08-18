package com.iyakovlev.task2.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.iyakovlev.task2.R
import com.iyakovlev.task2.databinding.FragmentContactDetailViewBinding
import com.iyakovlev.task2.domain.Contact
import com.iyakovlev.task2.utils.TestingConstants
import com.iyakovlev.task2.utils.TestingConstants.isUsingTransactions

class ContactDetailViewFragment : BaseFragment<FragmentContactDetailViewBinding>(FragmentContactDetailViewBinding::inflate) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setListeners()
//        setObservers()
    }

    override fun setObservers() {
        TODO("Not yet implemented")
    }

    override fun setListeners() {
        binding.btnBack.setOnClickListener {
            if (isUsingTransactions) {
                parentFragmentManager.popBackStack()
            } else {
                findNavController().popBackStack()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(contact: Contact) =
            ContactDetailViewFragment().apply {

            }
    }
}