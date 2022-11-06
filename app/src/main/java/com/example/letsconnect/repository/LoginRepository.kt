package com.example.letsconnect.repository

import android.content.Context
import android.util.Log
import com.example.letsconnect.Resource
import com.example.letsconnect.Response
import com.example.letsconnect.models.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue.serverTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val database: FirebaseFirestore,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {
    val auth: FirebaseAuth = FirebaseAuth.getInstance()

    suspend fun firebaseSignInWithGoogle(idToken: String) = flow {

        try {
            emit(Response.Loading)
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.additionalUserInfo?.apply {
                emit(Response.Success(isNewUser))
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
        }
    }

    suspend fun deleteAccount(idToken: String) = flow {

        try {
            emit(Response.Loading)
            val user = auth.currentUser;
            val credential = GoogleAuthProvider.getCredential(idToken, null)

            user?.reauthenticate(credential)?.addOnCompleteListener {
                Log.d("Login Repo", "account deleted: ")
                user.delete()
                auth.signOut()
            }

        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some error"))
            Log.d("Login Repo", "${e.message} ")
        }
    }

    suspend fun createUser() = flow {
        try {
            emit(Response.Loading)
            auth.currentUser?.apply {
                database.collection("users").document(uid).set(mapOf(
                    "name" to displayName,
                    "email" to email,
                    "followers" to ArrayList<String>(),
                    "following" to ArrayList<String>(),
                    "userId" to uid,
                    "username" to "No username"
                )).await().also {
                    emit(Response.Success(it))
                }
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some message"))
        }
    }

    suspend fun getCurrentUser() = flow {
        try {
            emit(Response.Loading)
            auth.currentUser?.apply {
                Log.d("LoginRep", "try: Current User loading: ")

                database.collection("users").document(uid).get().await().toObject(Users::class.java)
                    .also {
                        emit(Response.Success(it))
                    }
            }
        } catch (e: Exception) {
            emit(Response.Failure(e.message ?: "Some message"))
        }
    }


}
