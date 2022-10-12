package com.example.letsconnect.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.FirebaseRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: FirebaseRepository) : ViewModel() {

    // QuerySnapshot
    private var _allComments: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val allComments: LiveData<Resource<QuerySnapshot>> = _allComments

     private var _post: MutableLiveData<Resource<DocumentSnapshot>> = MutableLiveData()
    val post: LiveData<Resource<DocumentSnapshot>> = _post

     fun getAllComments(postId:String) = viewModelScope.launch(Dispatchers.IO){
        _allComments.postValue(Resource.Loading())
        _allComments.postValue(repository.getAllComments(postId))
    }

    fun addNewComment(postId: String,message:String) = viewModelScope.launch(Dispatchers.IO){
        repository.postComment(postId,message)
    }
    fun addNewPost(postMessage:String) = viewModelScope.launch(Dispatchers.IO){
        repository.addNewPost(postMessage)
    }

    fun getCurrentPost(postId: String) = viewModelScope.launch {
        _post.postValue(Resource.Loading())
        _post.postValue(repository.showCurrentPost(postId))
    }
}