package com.example.letsconnect.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LoginRepository  @Inject constructor(
    private val database: FirebaseFirestore,
    private var storageRef: StorageReference,
    private val auth: FirebaseAuth,
    @ApplicationContext private val context: Context,
) : BaseRepo(context) {

}
