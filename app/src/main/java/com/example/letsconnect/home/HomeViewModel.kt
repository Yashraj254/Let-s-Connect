package com.example.letsconnect.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.letsconnect.Resource
import com.example.letsconnect.repository.FirebaseRepository
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: FirebaseRepository) : ViewModel() {

    // QuerySnapshot
    private var _allPosts: MutableLiveData<Resource<QuerySnapshot>> = MutableLiveData()
    var allPosts: LiveData<Resource<QuerySnapshot>> = _allPosts

    fun getAllPosts() = viewModelScope.launch(Dispatchers.IO) {
        _allPosts.postValue(Resource.Loading())
        _allPosts.postValue(repository.getAllPosts())
    }
}