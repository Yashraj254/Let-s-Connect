package com.example.letsconnect.users

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllPostsFirestoreAdapter
import com.example.letsconnect.adapters.IPostAdapter
import com.example.letsconnect.databinding.FragmentProfileBinding
import com.example.letsconnect.models.Post
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class UserFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection("users")
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AllPostsFirestoreAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentProfileBinding.inflate(layoutInflater, container, false)

        //   val user = auth.currentUser.uid
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val selectedUser = arguments?.getString("selected_userId")
        if (selectedUser != null)
            showCurrentUser(selectedUser)
        else
            showCurrentUser(auth.currentUser!!.uid)
    }

    private fun showCurrentUser(userId: String) {
        database.collection(KEY_COLLECTION_USERS).document(userId).get().addOnSuccessListener {
            val name = it.getString(KEY_USER_NAME)
            val email = it.getString(KEY_EMAIL)
            val followers = it.getLong(KEY_FOLLOWERS)!!.toInt()
            val following = it.getLong(KEY_FOLLOWING)!!.toInt()
            // val image = it.getString(KEY_PROFILE_IMAGE)
            binding.apply {
                tvName.text = name
                tvUsername.text = email
                tvFollowers.text = "Followers: $followers"
                tvFollowing.text = "Following: $following"
//                if(image!=null)
//                Glide.with(requireContext()).load(image).into(ivProfileImage)
            }
        }
        val query = database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid)
            .collection("myPosts")
        setRecyclerView(query)
    }

    private fun setRecyclerView(query: Query) {
        val options: FirestoreRecyclerOptions<Post> =
            FirestoreRecyclerOptions.Builder<Post>().setQuery(query, Post::class.java).build()
        binding.rvMyPosts.layoutManager = LinearLayoutManager(context)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        adapter = AllPostsFirestoreAdapter(options,navHostFragment,"AllUsers")

        binding.rvMyPosts.adapter = adapter
    }

    override fun onStart() {
        super.onStart()
        adapter.startListening()

    }

    override fun onStop() {
        super.onStop()
        adapter.stopListening()
    }



}