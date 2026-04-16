package com.example.gifthub.ui.redeem

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import kotlinx.coroutines.launch

private val AccentOrange = Color(0xFFFF6B35)
private val AccentAmber = Color(0xFFFFB347)
private val DarkBg = Color(0xFF0F0F23)
private val DarkSurface = Color(0xFF1A1A2E)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0C0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemGiftScreen(
    onBack: () -> Unit,
    viewModel: RedeemGiftViewModel
) {
    var voucherCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val qrLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result ->
        result.contents?.let { scanned ->
            voucherCode = extractVoucherCode(scanned)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Redeem Gift", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
            )
        },
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "Scan voucher QR or enter code manually",
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = voucherCode,
                onValueChange = { voucherCode = it },
                label = { Text("Voucher code") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = {
                        val options = ScanOptions().apply {
                            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            setPrompt("Scan voucher QR")
                            setBeepEnabled(true)
                            setOrientationLocked(true)
                        }
                        qrLauncher.launch(options)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan QR")
                }

                Button(
                    enabled = voucherCode.isNotBlank() && !viewModel.isLoading,
                    onClick = {
                        scope.launch { viewModel.redeemByCode(voucherCode) }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(listOf(AccentOrange, AccentAmber)),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 18.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Redeem", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            viewModel.message?.let { msg ->
                val isSuccess = msg.contains("success", ignoreCase = true)
                val bg = if (isSuccess) Color(0xFF1B3A2A) else Color(0xFF3A1B1B)
                val fg = if (isSuccess) Color(0xFF81C784) else Color(0xFFFF8A80)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(bg, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(msg, color = fg)
                }
            }
        }
    }
}

private fun extractVoucherCode(raw: String): String {
    val regex = Regex("""[?&]code=([^&]+)""")
    val match = regex.find(raw)
    return match?.groupValues?.get(1) ?: raw
}