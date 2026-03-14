package com.example.gifthub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.gifthub.navigation.GiftHubNavGraph
import com.example.gifthub.ui.theme.GiftHubTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GiftHubTheme {
                GiftHubNavGraph()
            }
        }
    }
}