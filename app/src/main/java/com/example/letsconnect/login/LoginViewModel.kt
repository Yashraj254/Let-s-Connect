package com.example.letsconnect.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.letsconnect.repository.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(private val repository: LoginRepository) : ViewModel() {

    fun signInWithGoogle(idToken: String) = liveData(Dispatchers.IO) {
        Log.d("Login ViewModel", "sign in ")
        repository.firebaseSignInWithGoogle(idToken).collect { response ->
            emit(response)
        }
    }
    fun deleteAccount(idToken: String) = liveData(Dispatchers.IO) {
        Log.d("Login ViewModel", "delete account ")

        repository.deleteAccount(idToken).collect { response ->
            emit(response)
        }
    }

    fun createUser() = liveData(Dispatchers.IO) {
        repository.createUser().collect { response ->
            emit(response)
        }
    }

}