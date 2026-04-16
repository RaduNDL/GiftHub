package com.example.gifthub

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.gifthub.navigation.GiftHubNavGraph
import com.example.gifthub.screens.notifications.GiftHubMessagingService
import com.example.gifthub.screens.notifications.NotificationScheduler
import com.example.gifthub.ui.theme.GiftHubTheme

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            NotificationScheduler.schedulePeriodicNotifications(this)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GiftHubMessagingService.createNotificationChannel(this)
        requestNotificationPermission()

        setContent {
            GiftHubTheme {
                GiftHubNavGraph()
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    NotificationScheduler.schedulePeriodicNotifications(this)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            NotificationScheduler.schedulePeriodicNotifications(this)
        }
    }
}