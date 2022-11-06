package com.example.letsconnect.post

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.PostsRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostsRepository) : ViewModel() {

    // QuerySnapshot
    private var _allComments: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allComments: StateFlow<Resource<QuerySnapshot>> = _allComments

     private var _post: MutableStateFlow<Resource<DocumentSnapshot>> = MutableStateFlow(Resource.Loading())
    val post: StateFlow<Resource<DocumentSnapshot>> = _post

      private var _newPost: MutableStateFlow<Resource<Boolean>> = MutableStateFlow(Resource.Loading())
     val newPost: StateFlow<Resource<Boolean>> = _newPost

    private var _currentUserPosts: MutableStateFlow<Resource<QuerySnapshot>> =
        MutableStateFlow(Resource.Loading())
    val currentUserPosts: StateFlow<Resource<QuerySnapshot>> = _currentUserPosts

     fun getAllComments(postId:String) = viewModelScope.launch(Dispatchers.IO){
         _allComments.emit(Resource.Loading())
         _allComments.emit(repository.getAllComments(postId))
    }

    fun addNewComment(postId: String,message:String) = liveData(Dispatchers.IO){
       repository.postComment(postId,message).collect{
           response-> emit(response)
       }

    }

    fun addNewPost(postMessage:String) = liveData(Dispatchers.IO){
        repository.addNewPost(postMessage).collect{response->
            emit(response)
        }
    }

    fun getCurrentPost(postId: String) = viewModelScope.launch {
        _post.emit(Resource.Loading())
        _post.emit(repository.showCurrentPost(postId))
    }

    fun deleteComment(commentId:String,postId: String)= viewModelScope.launch{
        repository.deleteComment(commentId, postId)
    }

    fun getMyPosts() = liveData(Dispatchers.IO) {
        repository.showMyPosts().collect{
            response->emit(response)
        }
    }

}