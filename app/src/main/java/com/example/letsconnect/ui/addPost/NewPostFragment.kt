package com.example.letsconnect.ui.addPost

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.browser.trusted.sharing.ShareTarget.FileFormField.KEY_NAME
import androidx.fragment.app.Fragment
import com.example.letsconnect.*
import com.example.letsconnect.databinding.FragmentNewPostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import kotlin.collections.HashMap


class NewPostFragment : Fragment() {

    private var _binding: FragmentNewPostBinding? = null
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private val userRef = database.collection("users")

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentNewPostBinding.inflate(layoutInflater, container, false)

        binding.btnAddPost.setOnClickListener {
            if (auth.currentUser == null)
                startActivity(Intent(requireActivity(), LoginActivity::class.java))
            else {
                addNewPost()
            }
        }
        return binding.root
    }

    private fun addNewPost() {
        database.collection(KEY_COLLECTION_USERS).document(auth.currentUser!!.uid).get()
            .addOnSuccessListener {
                val date = Date()
                val username = it.getString(KEY_USER_NAME)
                val userId = auth.currentUser!!.uid
                val post = HashMap<String, Any>()
                post[KEY_USER_ID] = userId
                post[KEY_USER_NAME] = username!!
                post[KEY_EMAIL] = auth.currentUser!!.email!!
                post[KEY_TOTAL_VIEWS] = 0
                post[KEY_TOTAL_COMMENTS] = 0
                post[KEY_TOTAL_LIKES] = 0
                post[KEY_UPLOAD_TIME] = ""
                post[KEY_POST_MESSAGE] = binding.etPostMessage.text.toString()
                post["TIME_STAMP"] = date.time
                val postId = database.collection(KEY_ALL_POSTS).document()
                post["postId"] = postId.id
                postId.set(post).addOnSuccessListener {
                    Toast.makeText(context, "Post Uploaded", Toast.LENGTH_SHORT).show()
                    database.collection("users").document(userId).collection("myPosts")
                        .document(postId.id).set(post)
                    Log.i("NewPost", "addNewPost: $postId")
                }
                    .addOnFailureListener {
                        Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()
                    }
            }

    }
}