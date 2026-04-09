package com.example.gifthub.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseAuthProvider {
    val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
}

object FirebaseFirestoreProvider {
    val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}