package com.example.letsconnect.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.letsconnect.Post
import com.example.letsconnect.R
import com.example.letsconnect.databinding.RvPostItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AllPostsFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<Post>,
    private val navHostFragment: NavHostFragment
) :
    FirestoreRecyclerAdapter<Post, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Post) {
        (holder as PostViewHolder).bind(model)

    }

    inner class PostViewHolder(private val binding: RvPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post) {
            binding.apply {
                tvUsername.text = post.username
                tvEmail.text = post.email
                tvPostMessage.text = post.postMessage
                tvTotalComments.text = post.totalComments.toString()
                tvTotalLikes.text = post.totalLikes.toString()
                tvViews.text = "Views: ${post.totalViews.toString()}"
                ibtnLike.setOnClickListener {
                    ibtnLike.setImageResource(R.drawable.ic_liked_post)

                }
                tvEmail.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId",post.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_navigation_home_to_navigation_profile,bundle)
                }
                tvUsername.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_userId",post.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_navigation_home_to_navigation_profile,bundle)
                }
                ibtnComments.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_postId",post.postId)
                    bundle.putString("selected_email",post.email)
                    bundle.putString("selected_username",post.username)
                    bundle.putString("selected_userId",post.userId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_navigation_home_to_postFragment,bundle)
                }
            }
        }
    }
}