package com.example.letsconnect.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.R
import com.example.letsconnect.databinding.RvUserItemBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AllUsersFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<Users>,
) :
    FirestoreRecyclerAdapter<Users, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Users) {
        (holder as PostViewHolder).bind(model)
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("selected_userId", model.userId)
            Navigation.findNavController(it)
                .navigate(R.id.action_navigation_search_users_to_navigation_profile, bundle)
        }
    }

    inner class PostViewHolder(private val binding: RvUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(users: Users) {
            binding.apply {
                tvChatUserName.text = users.username
                tvChatUserEmail.text = users.email
                val image = users.profileImage
                if (image != null)
                    Glide.with(ivProfileImage).load(image).into(binding.ivProfileImage)

            }
        }
    }
}