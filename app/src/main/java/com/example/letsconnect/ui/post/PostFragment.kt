package com.example.letsconnect.ui.post

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.letsconnect.*
import com.example.letsconnect.adapters.AllCommentsFirestoreAdapter
import com.example.letsconnect.databinding.FragmentPostBinding
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import kotlin.collections.HashMap


class PostFragment : Fragment() {


    private var _binding: FragmentPostBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection(KEY_ALL_POSTS)
    private lateinit var adapter: AllCommentsFirestoreAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPostBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val postId = arguments?.getString("selected_postId")
        val username =  arguments?.getString("selected_username")
        val email =  arguments?.getString("selected_email")
        val userId =  arguments?.getString("selected_userId")
        showCurrentPost(postId!!)
        binding.btnComment.setOnClickListener {
            val message = binding.etComment.text.toString()
            postComment(postId,message,userId!!,username!!,email!!)
        }
        val query = userRef.document(postId.toString()).collection("comments").orderBy("TIME_STAMP")
        setRecyclerView(query)
    }
    private fun showCurrentPost(postId:String){
        database.collection(KEY_ALL_POSTS).document(postId).get().addOnSuccessListener {
            val name = it.getString("username")
            val email = it.getString("email")

            // val image = it.getString(KEY_PROFILE_IMAGE)
            binding.apply {
                tvUsername.text = name
                tvEmail.text = email

//                if(image!=null)
//                Glide.with(requireContext()).load(image).into(ivProfileImage)
            }
        }
    }
    private fun postComment(postId: String, message: String,userId:String,userName:String,userMail:String) {
        val comment = HashMap<String, Any>()
        comment[KEY_USER_ID] = userId
        comment[KEY_USER_NAME] = userName
        comment[KEY_EMAIL] = userMail
        comment["commentMessage"] = message
        val date = Date()
        comment["TIME_STAMP"] = date.time


        database.collection(KEY_ALL_POSTS).document(postId).collection("comments")
            .add(comment)
    }
    private fun setRecyclerView(query: Query) {

        val options: FirestoreRecyclerOptions<Comment> =
            FirestoreRecyclerOptions.Builder<Comment>().setQuery(query, Comment::class.java).build()
        binding.rvAllComments.layoutManager = LinearLayoutManager(context)
        val navHostFragment =
            requireActivity().supportFragmentManager.findFragmentById(R.id.container_fragment) as NavHostFragment
        adapter = AllCommentsFirestoreAdapter(options,navHostFragment)

        binding.rvAllComments.adapter = adapter
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