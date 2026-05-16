package com.skillexchange.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun PhoneAuthScreen(
    vm: AppViewModel,
    onNewUser: () -> Unit,
    onExistingUser: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme

    var step by remember { mutableIntStateOf(0) }
    var phone by remember { mutableStateOf("") }
    var countryCode by remember { mutableStateOf("+91") }
    var otp by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var countdown by remember { mutableIntStateOf(60) }
    var resendEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(step) {
        if (step == 1) {
            countdown = 60
            resendEnabled = false
            while (countdown > 0) {
                delay(1000L)
                countdown--
            }
            resendEnabled = true
        }
    }

    fun sendOtp() {
        val fullPhone = "$countryCode${phone.trim()}"
        loading = true
        error = ""
        vm.sendOtp(
            phoneNumber = fullPhone,
            activity = activity,
            onCodeSent = {
                loading = false
                step = 1
            },
            onAutoVerified = { isNew ->
                loading = false
                if (isNew) onNewUser() else onExistingUser()
            },
            onError = { msg ->
                loading = false
                error = msg
            }
        )
    }

    fun verifyOtp() {
        loading = true
        error = ""
        vm.verifyOtp(
            code = otp,
            onNewUser = { loading = false; onNewUser() },
            onExistingUser = { loading = false; onExistingUser() },
            onError = { msg -> loading = false; error = msg }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo
        Surface(
            shape = CircleShape,
            color = colors.primary,
            tonalElevation = 8.dp,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(
                    if (step == 0) Icons.Default.Phone else Icons.Default.Lock,
                    contentDescription = null,
                    tint = colors.onPrimary,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Skill Exchange", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
        Text("Trade your skills. Build your community.", color = colors.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(36.dp))

        // Steps
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(0, 1).forEach { i ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (i <= step) colors.primary else colors.outline,
                    modifier = Modifier.height(8.dp).width(if (i == step) 28.dp else 8.dp)
                ) {}
            }
        }
        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                AnimatedVisibility(
                    visible = step == 0,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Enter your mobile number", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "We will send a 6-digit OTP to verify your identity.",
                            color = colors.onSurfaceVariant, fontSize = 14.sp
                        )

                        // Phone input with country code
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { if (it.length <= 10 && it.all(Char::isDigit)) phone = it },
                            label = { Text("Mobile Number") },
                            placeholder = { Text("9876543210") },
                            prefix = {
                                Text(
                                    "$countryCode  |  ",
                                    color = colors.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = { Icon(Icons.Default.Phone, null, tint = colors.primary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Country code helper
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Info, null, modifier = Modifier.size(14.dp), tint = colors.onSurfaceVariant)
                            Text(
                                "Default: India (+91). Change country code if needed.",
                                color = colors.onSurfaceVariant, fontSize = 12.sp
                            )
                        }

                        if (error.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(10.dp), color = colors.errorContainer) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Warning, null, tint = colors.error, modifier = Modifier.size(16.dp))
                                    Text(error, color = colors.onErrorContainer, fontSize = 13.sp)
                                }
                            }
                        }

                        Button(
                            onClick = ::sendOtp,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = phone.length >= 8 && !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Sending OTP...")
                            } else {
                                Icon(Icons.Default.Send, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Send OTP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = step == 1,
                    enter = fadeIn() + slideInVertically()
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("Enter the OTP", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            "A 6-digit code was sent to $countryCode $phone",
                            color = colors.onSurfaceVariant, fontSize = 14.sp
                        )

                        OtpBoxInput(
                            otp = otp,
                            onOtpChange = { otp = it }
                        )

                        if (error.isNotEmpty()) {
                            Surface(shape = RoundedCornerShape(10.dp), color = colors.errorContainer) {
                                Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Icon(Icons.Default.Warning, null, tint = colors.error, modifier = Modifier.size(16.dp))
                                    Text(error, color = colors.onErrorContainer, fontSize = 13.sp)
                                }
                            }
                        }

                        Button(
                            onClick = ::verifyOtp,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = otp.length == 6 && !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Verifying...")
                            } else {
                                Icon(Icons.Default.Verified, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Verify & Continue", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }

                        // Resend + back
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = {
                                step = 0; otp = ""; error = ""
                            }) {
                                Icon(Icons.Default.ArrowBack, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Change number")
                            }
                            if (resendEnabled) {
                                TextButton(onClick = {
                                    otp = ""
                                    sendOtp()
                                }) {
                                    Text("Resend OTP", fontWeight = FontWeight.Bold, color = colors.primary)
                                }
                            } else {
                                Text(
                                    "Resend in ${countdown}s",
                                    color = colors.onSurfaceVariant, fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("1 hour of work = 1 Skill Point", color = colors.onSurfaceVariant, fontSize = 13.sp)
    }
}

@Composable
fun OtpBoxInput(otp: String, onOtpChange: (String) -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val colors = MaterialTheme.colorScheme

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxWidth()) {
        // Hidden text input captures keyboard
        BasicTextField(
            value = otp,
            onValueChange = { if (it.length <= 6 && it.all(Char::isDigit)) onOtpChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            textStyle = TextStyle(color = colors.background),
            cursorBrush = SolidColor(colors.background),
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester)
        )

        // 6 visual boxes
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.clickable {
                try { focusRequester.requestFocus() } catch (_: Exception) {}
            }
        ) {
            repeat(6) { index ->
                val char = otp.getOrNull(index)?.toString() ?: ""
                val isActive = otp.length == index
                val isFilled = char.isNotEmpty()

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            when {
                                isActive -> colors.primaryContainer
                                isFilled -> colors.primaryContainer.copy(alpha = 0.5f)
                                else -> colors.surfaceVariant
                            }
                        )
                        .border(
                            width = 2.dp,
                            color = when {
                                isActive -> colors.primary
                                isFilled -> colors.primary.copy(alpha = 0.5f)
                                else -> colors.outline
                            },
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Text(
                        text = char.ifEmpty { if (isActive) "│" else "" },
                        fontSize = if (char.isEmpty()) 18.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colors.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
