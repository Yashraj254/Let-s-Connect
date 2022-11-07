package com.example.letsconnect.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.databinding.RvSearchUserItemBinding
import com.example.letsconnect.models.Users
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class AllUsersAdapter(
    private val usersList: ArrayList<Users>,
    private val userItemClicked: OnSearchUserItemClicked,
    private val currentUser: String,
) :
    RecyclerView.Adapter<AllUsersAdapter.UserViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding =
            RvSearchUserItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        binding.onSearchUserItemClicked = userItemClicked
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    inner class UserViewHolder(private val binding: RvSearchUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val user = usersList[position]
            binding.position = position

            binding.apply {
                tvChatName.text = user.name
                tvChatUserName.text = user.username
                val image = user.profileImage
                if (image != null)
                    Glide.with(ivProfileImage).load(image).into(binding.ivProfileImage)
            }
        }
    }

    interface OnSearchUserItemClicked {
        fun onUserClicked(position: Int)
    }
}