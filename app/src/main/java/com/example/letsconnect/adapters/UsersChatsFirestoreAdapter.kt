package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvUserItemBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UsersChatsFirestoreAdapter(
    options: FirestoreRecyclerOptions<Users>,
    private val userItemClicked: OnUserItemClicked
) :
    FirestoreRecyclerAdapter<Users, RecyclerView.ViewHolder>(options) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = RvUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onUserItemClicked = userItemClicked
        return UserChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, model: Users) {
        (holder as UserChatViewHolder).bind(model,position)

    }

    inner class UserChatViewHolder(private val binding: RvUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: Users,position: Int) {
            binding.users = user
            binding.position = position
            binding.apply {
                tvChatUserName.text = user.username
                tvChatUserEmail.text = user.email
                val image = user.profileImage
                if (image != null)
                    Glide.with(ivProfileImage).load(image).into(ivProfileImage)
            }
        }
    }

    interface OnUserItemClicked {
        fun onUserChatClicked(position: Int)
    }
}