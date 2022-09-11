package com.example.letsconnect.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.letsconnect.Comment
import com.example.letsconnect.R
import com.example.letsconnect.databinding.RvCommentItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions


class AllCommentsFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<Comment>,

    private val navHostFragment: NavHostFragment
) :
    FirestoreRecyclerAdapter<Comment, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvCommentItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Comment) {
        (holder as PostViewHolder).bind(model)

    }

    inner class PostViewHolder(private val binding: RvCommentItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                tvUsername.text = comment.username
                tvUsermail.text = comment.email
                tvComment.text = comment.commentMessage
                tvUsermail.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId",comment.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_postFragment_to_navigation_profile,bundle)
                }
                tvUsername.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId",comment.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_postFragment_to_navigation_profile,bundle)
                }

            }
        }
    }
}