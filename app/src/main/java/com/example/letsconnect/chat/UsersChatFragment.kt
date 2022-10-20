package com.example.letsconnect.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.UsersChatsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentUsersChatBinding
import com.example.letsconnect.models.Users
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.firebase.ui.firestore.ObservableSnapshotArray
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UsersChatFragment : Fragment(), UsersChatsFirestoreAdapter.OnUserItemClicked {
    private var _binding: FragmentUsersChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var currentUser: String
    private lateinit var adapter: UsersChatsFirestoreAdapter
    private lateinit var navBar: BottomNavigationView
    private val viewModel: ChatViewModel by activityViewModels()
    private lateinit var arr: ObservableSnapshotArray<Users>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentUsersChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "All Chats"
        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE
        setRecyclerView(currentUser)
    }

    private fun setRecyclerView(userId: String) {
        viewModel.getAllFollowing(userId)
        lifecycleScope.launchWhenCreated {
            viewModel.allFollowing.collect {
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            rvUsersChats.isVisible = false
                            pbLoading.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            statusBox.isVisible = false
                            rvUsersChats.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                        }
                        if (!it.data!!.isEmpty) {
                            binding.rvUsersChats.isVisible = true
                            val options: FirestoreRecyclerOptions<Users> =
                                FirestoreRecyclerOptions.Builder<Users>()
                                    .setQuery(it.data.query, Users::class.java)
                                    .build()
                            arr = options.snapshots
                            binding.rvUsersChats.layoutManager = LinearLayoutManager(context)
                            adapter = UsersChatsFirestoreAdapter(options, this@UsersChatFragment)
                            binding.rvUsersChats.adapter = adapter
                            adapter.startListening()
                        }
                    }
                }
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        navBar.visibility = View.VISIBLE
        if (this::adapter.isInitialized)
            adapter.stopListening()
        _binding = null
    }

    override fun onUserChatClicked(position: Int) {
        val bundle = Bundle()
        bundle.putString("receiver_id", arr[position].userId)
        bundle.putString("receiver_name", arr[position].username)
        Navigation.findNavController(requireView())
            .navigate(R.id.action_usersChatFragment_to_chatFragment, bundle)
    }

}