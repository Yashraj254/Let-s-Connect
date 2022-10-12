package com.example.letsconnect.adapters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.letsconnect.R
import com.example.letsconnect.models.Users
import com.example.letsconnect.databinding.RvUserItemBinding
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class UsersChatsFirestoreAdapter(
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
            bundle.putString("receiver_id",model.userId)
            bundle.putString("receiver_name",model.username)
            Navigation.findNavController(it).navigate(R.id.action_usersChatFragment_to_chatFragment,bundle)
        }
    }

    inner class PostViewHolder(private val binding: RvUserItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Users) {
            binding.apply {
                tvChatUserName.text = user.username
                tvChatUserEmail.text = user.email
                val image = user.profileImage
                if(image!=null)
                    Glide.with(ivProfileImage).load(image).into(ivProfileImage)
            }
        }
    }
}