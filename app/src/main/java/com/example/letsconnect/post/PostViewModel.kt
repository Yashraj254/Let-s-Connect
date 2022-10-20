package com.example.letsconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.PostsRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostsRepository) : ViewModel() {

    // QuerySnapshot
    private var _allComments: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allComments: StateFlow<Resource<QuerySnapshot>> = _allComments

     private var _post: MutableStateFlow<Resource<DocumentSnapshot>> = MutableStateFlow(Resource.Loading())
    val post: StateFlow<Resource<DocumentSnapshot>> = _post

     fun getAllComments(postId:String) = viewModelScope.launch(Dispatchers.IO){
        _allComments.emit(repository.getAllComments(postId))
    }

    fun addNewComment(postId: String,message:String) = viewModelScope.launch(Dispatchers.IO){
        repository.postComment(postId,message)
    }
    fun addNewPost(postMessage:String) = viewModelScope.launch(Dispatchers.IO){
        repository.addNewPost(postMessage)
    }

    fun getCurrentPost(postId: String) = viewModelScope.launch {
        _post.emit(repository.showCurrentPost(postId))
    }


}