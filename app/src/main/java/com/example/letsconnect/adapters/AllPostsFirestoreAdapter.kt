package com.example.letsconnect.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.KEY_ALL_POSTS
import com.example.letsconnect.models.Post
import com.example.letsconnect.R
import com.example.letsconnect.Utils
import com.example.letsconnect.databinding.RvPostItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class AllPostsFirestoreAdapter(
    options: FirestoreRecyclerOptions<Post>,
    private val navHostFragment: NavHostFragment,
    private val from: String,
) :
    FirestoreRecyclerAdapter<Post, RecyclerView.ViewHolder>(options) {
    val currentUser = FirebaseAuth.getInstance().currentUser!!.uid
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvPostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Post) {
        (holder as PostViewHolder).bind(model)
        holder.getLikedPosts(model, holder.ibtnLike)
    }

    inner class PostViewHolder(private val binding: RvPostItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val ibtnLike = binding.ibtnLike
        fun bind(post: Post) {
            binding.ibtnLike.setOnClickListener {
                val list = post.likedBy
                val isLiked = list.contains(currentUser)
                if (isLiked) {
                    post.likedBy.remove(currentUser)
                    ibtnLike.setImageResource(R.drawable.ic_like)
                    FirebaseFirestore.getInstance().collection("users").document(currentUser)
                        .collection("likedPosts").document(post.postId).delete()
                } else {
                    post.likedBy.add(currentUser)
                    ibtnLike.setImageResource(R.drawable.ic_liked_post)
                    FirebaseFirestore.getInstance().collection("users").document(currentUser)
                        .collection("likedPosts").document(post.postId).set(post)

                }
                FirebaseFirestore.getInstance().collection(KEY_ALL_POSTS).document(post.postId)
                    .set(post)

            }
            binding.apply {
                tvUsername.text = post.username
                tvEmail.text = post.email
                tvPostMessage.text = post.postMessage
                tvTotalComments.text = post.totalComments.toString()
                tvTotalLikes.text = post.likedBy.size.toString()
                tvUploadTime.text = Utils.getTimeAgo(post.uploadTime).toString()
                val image = post.profileImage
                if (image != null)
                    Glide.with(imageView).load(image).into(imageView)

                tvEmail.setOnClickListener {
                    sendData(post)
                }

                tvUsername.setOnClickListener {
                 sendData(post)
                }

                ibtnComments.setOnClickListener {
                    val bundle = Bundle()
                    bundle.putString("selected_postId", post.postId)
                    val navController: NavController = navHostFragment.navController
                    navController.navigate(R.id.action_navigation_home_to_postFragment, bundle)

                    when (from) {
                        "Home" -> navController.navigate(R.id.action_navigation_home_to_postFragment,
                            bundle)
                        "LikedPosts" -> navController.navigate(R.id.action_navigation_liked_posts_to_postFragment,
                            bundle)
                    }
                }
            }
        }
    fun sendData(post:Post){
        val bundle = Bundle()
        bundle.putString("selected_userId", post.userId)
        val navController: NavController = navHostFragment.navController
        when (from) {
            "Home" -> navController.navigate(R.id.action_navigation_home_to_navigation_profile,
                bundle)
            "LikedPosts" -> navController.navigate(R.id.action_navigation_liked_posts_to_navigation_profile,
                bundle)
        }
    }
        fun getLikedPosts(
            post: Post,
            ibtnLike: ImageButton,

            ) {
            val list = post.likedBy
            val isLiked = list.contains(currentUser)
            if (isLiked) {
                ibtnLike.setImageResource(R.drawable.ic_liked_post)
            } else {
                ibtnLike.setImageResource(R.drawable.ic_like)
            }
        }
    }
}

interface IPostAdapter {
    fun onLikeClicked(postId: String)

}