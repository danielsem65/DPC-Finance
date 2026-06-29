package com.semdev.dpc.admin

import android.app.Application
import com.google.firebase.FirebaseApp

class AdminApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
