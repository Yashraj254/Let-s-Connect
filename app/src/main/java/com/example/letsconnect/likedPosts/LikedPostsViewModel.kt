package com.example.letsconnect.likedPosts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Post
import com.example.letsconnect.repository.PostsRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedPostsViewModel @Inject constructor(private val repository: PostsRepository) : ViewModel() {

    private var _likedPosts: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val likedPosts: StateFlow<Resource<QuerySnapshot>> = _likedPosts

    fun getLikedPosts() = viewModelScope.launch(Dispatchers.IO) {
        _likedPosts.emit(repository.getLikedPosts())
    }

    fun likePost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        repository.likePost(post)
    }
}