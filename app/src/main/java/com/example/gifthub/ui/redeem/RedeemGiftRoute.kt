package com.example.gifthub.ui.redeem

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RedeemGiftRoute(
    onBack: () -> Unit
) {
    val vm: RedeemGiftViewModel = viewModel()
    RedeemGiftScreen(
        onBack = onBack,
        viewModel = vm
    )
}