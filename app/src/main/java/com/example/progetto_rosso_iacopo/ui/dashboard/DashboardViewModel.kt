package com.example.progetto_rosso_iacopo.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth

class DashboardViewModel: ViewModel() {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val _userName: MutableLiveData<String> = MutableLiveData(auth.currentUser?.displayName ?: "Atleta")
    val userName: LiveData<String> = _userName
}