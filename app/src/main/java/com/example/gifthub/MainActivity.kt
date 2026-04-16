package com.example.gifthub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.gifthub.navigation.GiftHubNavGraph
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.ui.theme.GiftHubTheme

class MainActivity : ComponentActivity() {

    private var startupRoute by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        startupRoute = NotificationHelper.extractTargetRoute(intent)

        setContent {
            GiftHubTheme {
                GiftHubNavGraph(startupRoute = startupRoute)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        startupRoute = NotificationHelper.extractTargetRoute(intent)
    }
}