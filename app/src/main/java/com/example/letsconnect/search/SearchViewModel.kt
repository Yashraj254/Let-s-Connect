package com.example.letsconnect.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.example.letsconnect.Resource
import com.example.letsconnect.models.Users
import com.example.letsconnect.repository.UserRepository
import com.google.firebase.firestore.QuerySnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(private val repository: UserRepository) :
    ViewModel() {

    private var _allUsers: MutableStateFlow<Resource<QuerySnapshot>> = MutableStateFlow(Resource.Loading())
    val allUsers: StateFlow<Resource<QuerySnapshot>> = _allUsers

    fun getAllUsers() = liveData(Dispatchers.IO) {
        (repository.getAllUsers().collect{
            response->emit(response)
        })
    }

}
