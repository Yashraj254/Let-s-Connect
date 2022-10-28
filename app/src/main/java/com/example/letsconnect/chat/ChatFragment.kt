package com.example.letsconnect.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.Resource
import com.example.letsconnect.adapters.ChatsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentChatBinding
import com.example.letsconnect.models.ChatMessage
import com.example.letsconnect.showSnackBar
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var currentUser: String
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var receiverName: String
    private lateinit var adapter: ChatsFirestoreAdapter
    private lateinit var navBar: BottomNavigationView
    private val viewModel: ChatViewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navBar = requireActivity().findViewById(R.id.nav_view)
        navBar.visibility = View.GONE

        senderUid = currentUser
        receiverUid = arguments?.getString("receiver_id")!!
        receiverName = arguments?.getString("receiver_name")!!

        val actionBar =  requireActivity().findViewById<MaterialToolbar>(R.id.materialToolbar);
        actionBar.title = receiverName

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        setRecyclerView()

        binding.ibSend.setOnClickListener {
            if (!binding.etChatMessage.text.isNullOrEmpty()) {
                viewModel.sendAndReceiveMessage(binding.etChatMessage.text.toString(), receiverUid)
                binding.etChatMessage.text = null
            }
        }
    }

    private fun setRecyclerView() {
        viewModel.getAllChats(senderRoom)
        lifecycleScope.launchWhenCreated {
            viewModel.allChats.collect {
                when (it) {
                    is Resource.Error -> {
                        showSnackBar(message = it.message!!)
                        binding.apply {
                            statusBox.isVisible = true
                            rvAllChats.isVisible = false
                            pbLoading.isVisible = false
                            etChatMessage.isVisible = false
                            ibSend.isVisible = false
                        }
                    }
                    is Resource.Loading -> {
                        binding.apply {
                            statusBox.isVisible = false
                            rvAllChats.isVisible = false
                            pbLoading.isVisible = true
                        }
                    }
                    is Resource.Success -> {
                        binding.apply {
                            statusBox.isVisible = false
                            pbLoading.isVisible = false
                        }

                        if (!it.data!!.isEmpty) {
                            val options: FirestoreRecyclerOptions<ChatMessage> =
                                FirestoreRecyclerOptions.Builder<ChatMessage>()
                                    .setQuery(it.data.query, ChatMessage::class.java).build()
                            binding.rvAllChats.layoutManager = LinearLayoutManager(context)

                            adapter = ChatsFirestoreAdapter(options)
                            binding.rvAllChats.adapter = adapter
                            adapter.startListening()
                            binding.apply {
                                rvAllChats.isVisible = true
                                etChatMessage.isVisible = true
                                ibSend.isVisible = true
                            }

                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (this::adapter.isInitialized)
            adapter.stopListening()
        _binding = null
    }
}