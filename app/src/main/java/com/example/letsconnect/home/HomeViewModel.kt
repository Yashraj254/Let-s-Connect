package com.example.letsconnect.home

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
class HomeViewModel @Inject constructor(private val repository: PostsRepository) : ViewModel() {

    // QuerySnapshot
    private var _allPosts: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    var allPosts: StateFlow<Resource<QuerySnapshot>> = _allPosts

    fun getAllPosts() = viewModelScope.launch(Dispatchers.IO) {
        _allPosts.emit(repository.getAllPosts())
    }

    fun likePost(post: Post) = viewModelScope.launch(Dispatchers.IO) {
        repository.likePost(post)
    }
}