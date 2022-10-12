package com.example.letsconnect.chat

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.adapters.ChatsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentChatBinding
import com.example.letsconnect.models.ChatMessage
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*


class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val databaseRef = database.collection("chats")
    private lateinit var senderUid: String
    private lateinit var receiverUid: String
    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String
    private lateinit var receiverName: String
    private lateinit var adapter: ChatsFirestoreAdapter
    private lateinit var navBar: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navBar = requireActivity().findViewById(R.id.nav_view)
        senderUid = auth.uid.toString()
        receiverUid = arguments?.getString("receiver_id")!!
        receiverName = arguments?.getString("receiver_name")!!
        requireActivity().title = receiverName
        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        navBar.visibility = View.GONE
        setRecyclerView()

        binding.ibSend.setOnClickListener {
            if (!binding.etChatMessage.text.isNullOrEmpty()) {

                val message = HashMap<String, Any>()
                message["message"] = binding.etChatMessage.text.toString()
                message["senderId"] = senderUid
                message["timeStamp"] = Date().time
                message["sentTime"] = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                val randomKey = databaseRef.document().id
                databaseRef.document(senderRoom).collection("message")
                    .document(randomKey).set(message)
                databaseRef.document(receiverRoom).collection("message")
                    .document(randomKey).set(message)
                binding.etChatMessage.text = null
            }
        }

    }

    private fun setRecyclerView() {
        val query = databaseRef.document(senderRoom).collection("message").orderBy("timeStamp")
        val options: FirestoreRecyclerOptions<ChatMessage> =
            FirestoreRecyclerOptions.Builder<ChatMessage>().setQuery(query, ChatMessage::class.java).build()
            binding.rvAllChats.layoutManager = LinearLayoutManager(context)

        adapter = ChatsFirestoreAdapter(options)
        binding.rvAllChats.adapter = adapter

        binding.apply {
            rvAllChats.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
                // Wait till recycler_view will update itself and then scroll to the end.
                rvAllChats.post {
                    adapter.itemCount.takeIf { it > 0 }?.let {
                        rvAllChats.scrollToPosition(it - 1)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()
    }

    override fun onStop() {
        super.onStop()
        navBar.visibility = View.VISIBLE
        adapter.stopListening()
    }

}