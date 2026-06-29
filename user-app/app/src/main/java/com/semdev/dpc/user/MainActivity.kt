package com.semdev.dpc.user

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.semdev.dpc.user.ui.TouchBaseApp
import com.semdev.dpc.user.ui.theme.TouchBaseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TouchBaseTheme {
                TouchBaseApp()
            }
        }
    }
}
