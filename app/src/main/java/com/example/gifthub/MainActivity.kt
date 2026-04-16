package com.example.gifthub

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.gifthub.navigation.GiftHubNavGraph
import com.example.gifthub.screens.notifications.NotificationHelper
import com.example.gifthub.screens.notifications.NotificationRealtimeListener
import com.example.gifthub.screens.notifications.PushTokenManager
import com.example.gifthub.ui.theme.GiftHubTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainActivity : ComponentActivity() {

    private val _notificationRouteFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val notificationRouteFlow = _notificationRouteFlow.asSharedFlow()

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        FirebaseApp.initializeApp(this)
        NotificationHelper.ensureChannels(this)
        PushTokenManager.syncCurrentToken()
        PushTokenManager.subscribeDefaultTopics()
        requestNotificationPermissionIfNeeded()

        NotificationRealtimeListener.start(this)

        NotificationHelper.extractTargetRoute(intent)?.let {
            _notificationRouteFlow.tryEmit(it)
        }

        setContent {
            GiftHubTheme {
                GiftHubNavGraph(notificationRouteFlow = notificationRouteFlow)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        NotificationHelper.extractTargetRoute(intent)?.let {
            _notificationRouteFlow.tryEmit(it)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationRealtimeListener.stop()
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}