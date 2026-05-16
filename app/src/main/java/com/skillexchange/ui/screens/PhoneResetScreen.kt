package com.skillexchange.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneResetScreen(vm: AppViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    val activity = context as Activity
    val colors = MaterialTheme.colorScheme

    var step by remember { mutableIntStateOf(0) } // 0: Phone, 1: OTP, 2: New Password
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    fun sendOtp() {
        val fullPhone = "+91${phone.trim()}"
        loading = true
        error = ""
        vm.sendOtp(
            phoneNumber = fullPhone,
            activity = activity,
            onCodeSent = { loading = false; step = 1 },
            onAutoVerified = { _ -> loading = false; step = 2 },
            onError = { msg -> loading = false; error = msg }
        )
    }

    fun verifyOtp() {
        loading = true
        error = ""
        vm.verifyOtp(
            code = otp,
            onNewUser = { loading = false; step = 2 },
            onExistingUser = { loading = false; step = 2 },
            onError = { msg -> loading = false; error = msg }
        )
    }

    fun resetPassword() {
        if (newPassword != confirmPassword) {
            error = "Passwords do not match"
            return
        }
        loading = true
        vm.updatePassword(newPassword) { success, msg ->
            loading = false
            if (success) {
                vm.showSuccess("Password reset successfully! Please login.")
                onBack()
            } else {
                error = msg ?: "Failed to reset password"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reset Password") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Logo
            Surface(shape = CircleShape, color = colors.secondary, modifier = Modifier.size(64.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.VpnKey, null, tint = colors.onSecondary, modifier = Modifier.size(32.dp))
                }
            }
            Spacer(Modifier.height(24.dp))

            when (step) {
                0 -> {
                    Text("Forgot your password?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Enter your registered phone number to verify your identity.", 
                        color = colors.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { if (it.length <= 10) phone = it },
                        label = { Text("Phone Number") },
                        prefix = { Text("+91 ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = ::sendOtp, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), enabled = phone.length >= 10 && !loading) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
                        else Text("Send OTP")
                    }
                }
                1 -> {
                    Text("Verify Phone", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Enter the 6-digit code sent to +91 $phone", color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    OtpBoxInput(otp = otp, onOtpChange = { otp = it })
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = ::verifyOtp, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), enabled = otp.length == 6 && !loading) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
                        else Text("Verify OTP")
                    }
                }
                2 -> {
                    Text("New Password", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text("Set a secure password for your account.", color = colors.onSurfaceVariant)
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = ::resetPassword, modifier = Modifier.fillMaxWidth().height(52.dp), shape = RoundedCornerShape(12.dp), enabled = newPassword.length >= 6 && !loading) {
                        if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
                        else Text("Reset Password")
                    }
                }
            }

            if (error.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(error, color = colors.error, fontSize = 14.sp)
            }
        }
    }
}
