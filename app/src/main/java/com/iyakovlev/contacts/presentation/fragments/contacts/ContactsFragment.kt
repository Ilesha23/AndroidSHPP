package com.iyakovlev.contacts.presentation.fragments.contacts

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.iyakovlev.contacts.R
import com.iyakovlev.contacts.common.constants.Constants.ISDEBUG
import com.iyakovlev.contacts.common.resource.Resource
import com.iyakovlev.contacts.databinding.FragmentContactsBinding
import com.iyakovlev.contacts.presentation.activity.main.MainActivity
import com.iyakovlev.contacts.presentation.base.BaseFragment
import com.iyakovlev.contacts.presentation.fragments.contacts.adapters.ContactsAdapter
import com.iyakovlev.contacts.presentation.fragments.contacts.interfaces.ContactItemClickListener
import com.iyakovlev.contacts.presentation.utils.ItemSpacingDecoration
import com.iyakovlev.contacts.presentation.utils.extensions.addSwipe
import com.iyakovlev.contacts.presentation.utils.extensions.showSnackBarWithTimer
import com.iyakovlev.contacts.presentation.utils.extensions.toggleLoading
import com.iyakovlev.contacts.utils.log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ContactsFragment : BaseFragment<FragmentContactsBinding>(FragmentContactsBinding::inflate) {

    private val viewModel: ContactsViewModel by viewModels()

    private val contactAdapter = ContactsAdapter(object : ContactItemClickListener {

        override fun onItemClick(id: Long, imageView: ImageView) {
            navigateToDetailView(id, imageView)
        }

        override fun onItemDeleteClick(id: Long) {
            removeContactWithUndo(id)
        }

        override fun onItemLongClick(position: Int) {
            viewModel.changeSelectionState(true)
            viewModel.toggleSelectedPosition(position)
        }

        override fun onItemClick(position: Int) {
            viewModel.toggleSelectedPosition(position)
        }

    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.pbContacts.toggleLoading(true)
        setupRecyclerView()
        setListeners()
        setObservers()

    }

    private fun removeContactWithUndo(id: Long) {
        viewModel.deleteContact(id)
        showUndoDeleteSnackBar(getString(R.string.contact_deleted_snackbar)) {
            viewModel.undoRemoveContact()
        }
    }

    private fun removeContactListWithUndo() {
        viewModel.removeSelectedContacts()
        showUndoDeleteSnackBar(getString(R.string.contact_list_deleted_snackbar)) {
            viewModel.undoRemoveContactsList()
        }
    }

    private fun makeBinButton() {
        binding.fabUp.show()
        binding.rvContacts.clearOnScrollListeners()
        binding.fabUp.icon = ContextCompat.getDrawable(requireContext(), R.drawable.fab_bin)
    }

    private fun makeUpButton() {
        binding.fabUp.icon =
            ContextCompat.getDrawable(requireContext(), R.drawable.floating_action_button_up)
        val layoutManager = binding.rvContacts.layoutManager as LinearLayoutManager
        val firstItem = layoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstItem == 0) {
            binding.fabUp.hide()
        } else {
            binding.fabUp.show()
        }
        binding.rvContacts.apply {
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val fi = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (fi == 0) {
                        binding.fabUp.hide()
                    } else if (dy > 0) {
                        binding.fabUp.show()
                    }
                }
            })
        }
    }

    private fun setupRecyclerView() {
        binding.rvContacts.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contactAdapter
            val spacing = resources.getDimensionPixelSize(R.dimen.contacts_item_spacing)
            val lastSpacing = resources.getDimensionPixelSize(R.dimen.last_item_bottom_spacing)
            if (itemDecorationCount == 0) {
                addItemDecoration(ItemSpacingDecoration(spacing, lastSpacing))
            }
            addSwipe<ContactsAdapter.ContactViewHolder> {
                val id = viewModel.state.value.data?.get(it.bindingAdapterPosition)?.id
                if (id != null) {
                    removeContactWithUndo(id)
                }
            }
        }
    }

    private fun setObservers() {
        viewModel.updateContacts()
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.cachedList.collect {
                        log("cached list submitted: ${viewModel.cachedList.value}", ISDEBUG)
                        contactAdapter.submitList(it)
                        toggleSearchInfo(it)
                    }
                }
                launch {
                    viewModel.state.collect { list ->
                        log("contacts list submit", ISDEBUG)
                        contactAdapter.submitList(list.data)
                        if (viewModel.state.value is Resource.Error) {
                            binding.pbContacts.toggleLoading(false)
                            Toast.makeText(
                                context,
                                getString(viewModel.state.value.message ?: R.string.error),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (viewModel.state.value is Resource.Success) {
                            binding.pbContacts.toggleLoading(false)
                        }
                        list.data?.let { toggleSearchInfo(it) }
                    }
                }
                launch {
                    viewModel.isMultiSelect.collect {
                        contactAdapter.changeSelectionState(it)
                        if (it) {
                            makeBinButton()
                        } else {
                            makeUpButton()
                        }
                    }
                }
                launch {
                    viewModel.selectedPositions.collect {
                        contactAdapter.changeSelectedPositions(it)
                    }
                }

            }
        }
    }

    private fun setListeners() {
        with(binding) {
            btnAddContact.setOnClickListener {
                navController.navigate(ContactsFragmentDirections.actionContactsFragmentToAddContactFragment())
            }
            fabUp.setOnClickListener {
                if (!viewModel.isMultiSelect.value) {
                    rvContacts.smoothScrollToPosition(0)
                    binding.fabUp.hide()
                } else {
                    removeContactListWithUndo()
                    contactAdapter.changeSelectedPositions(emptyList())
                    viewModel.changeSelectionState(false)
                }
            }
            ibBack.setOnClickListener {
                navController.navigateUp()
            }
            ivContacts.setOnClickListener {
                // TODO: notification
                showNotification()
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun showNotification() {
        Toast.makeText(requireContext(), "clicked", Toast.LENGTH_SHORT).show()
        val notificationManager = NotificationManagerCompat.from(requireContext())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT)
            )
        }
        val intent = Intent(requireContext(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP// or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(requireContext(), "channel_id")
            .setContentTitle(getString(R.string.notification_click_to_search))
            .setSmallIcon(R.drawable.app_icon)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .addAction(R.drawable.app_icon, getString(R.string.search), pendingIntent)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        notificationManager.notify(1, notification.build())
    }


    private fun navigateToDetailView(id: Long, imageView: ImageView) {
        val contact = viewModel.state.value.data?.find {
            it.id == id
        }
        navController.navigate(
            ContactsFragmentDirections.actionContactsFragmentToContactDetailViewFragment(
                contact?.image ?: "",
                contact?.name ?: getString(R.string.default_name_main),
                contact?.career ?: getString(R.string.career_placeholder),
                contact?.address ?: getString(R.string.address)
            )
        )
    }

    @SuppressLint("ShowToast")
    private fun showUndoDeleteSnackBar(message: String, action: () -> Unit) {
        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
            .showSnackBarWithTimer(getString(R.string.undo_remove_snackbar)) {
                action()
            }
    }

    private fun toggleSearchInfo(list: List<*>) {
        with(binding) {
            if (list.isEmpty()) {
                tvSearchNotFound.visibility = View.VISIBLE
                tvSearchRecommendation.visibility = View.VISIBLE
            } else {
                tvSearchNotFound.visibility = View.INVISIBLE
                tvSearchRecommendation.visibility = View.INVISIBLE
            }
        }
    }

}