package com.example.letsconnect.likedPosts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.FirebaseRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LikedPostsViewModel @Inject constructor(private val repository: FirebaseRepository) : ViewModel() {

    private var _likedPosts: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    val likedPosts: LiveData<Resource<QuerySnapshot>> = _likedPosts

    fun getLikedPosts() = viewModelScope.launch(Dispatchers.IO) {
        _likedPosts.postValue(Resource.Loading())
        _likedPosts.postValue(repository.getLikedPosts())
    }
}