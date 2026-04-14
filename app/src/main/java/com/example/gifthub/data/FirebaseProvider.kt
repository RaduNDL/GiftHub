package com.example.gifthub.data

import com.google.firebase.auth.FirebaseAuth

object FirebaseAuthProvider {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}

