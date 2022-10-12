package com.example.letsconnect.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.R
import com.example.letsconnect.models.Users
import com.example.letsconnect.adapters.UsersChatsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentUsersChatBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class UsersChatFragment : Fragment() {
    private var _binding: FragmentUsersChatBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection("users")
    private lateinit var adapter: UsersChatsFirestoreAdapter
    private lateinit var navBar: BottomNavigationView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUsersChatBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = "All Chats"
        navBar = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
        navBar.visibility = View.GONE
        setRecyclerView()
    }

    private fun setRecyclerView() {
        val query = userRef.document(auth.currentUser!!.uid).collection("following")
        val options: FirestoreRecyclerOptions<Users> =
            FirestoreRecyclerOptions.Builder<Users>().setQuery(query, Users::class.java).build()
        binding.rvUsersChats.layoutManager = LinearLayoutManager(context)
        adapter = UsersChatsFirestoreAdapter(options)
        binding.rvUsersChats.adapter = adapter
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