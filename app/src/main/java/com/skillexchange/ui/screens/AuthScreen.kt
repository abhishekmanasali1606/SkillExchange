package com.skillexchange.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

enum class AuthMode { LOGIN, REGISTER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    vm: AppViewModel,
    onAuthSuccess: () -> Unit,
    onSetupRequired: () -> Unit,
    onPhoneResetClick: () -> Unit,
    onTermsClick: () -> Unit
) {
    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var message by remember { mutableStateOf<String?>(null) }
    var passwordVisible by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.SwapCalls,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = colors.primary
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = when(mode) {
                AuthMode.LOGIN -> "Welcome Back"
                AuthMode.REGISTER -> "Create Account"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )
        Text(
            text = when(mode) {
                AuthMode.LOGIN -> "Login to trade your skills"
                AuthMode.REGISTER -> "Join the skill exchange community"
            },
            fontSize = 14.sp,
            color = colors.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        if (error != null) {
            Text(error!!, color = colors.error, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        }
        if (message != null) {
            Text(message!!, color = colors.primary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; error = null },
            label = { Text("Email Address") },
            leadingIcon = { Icon(Icons.Default.Email, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = null },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        if (mode == AuthMode.REGISTER) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it; error = null },
                label = { Text("Confirm Password") },
                leadingIcon = { Icon(Icons.Default.LockClock, null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
        }

        if (mode == AuthMode.LOGIN) {
            TextButton(
                onClick = { onPhoneResetClick() },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Forgot Password?")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (email.isBlank()) { error = "Email is required"; return@Button }
                loading = true
                error = null
                message = null

                when (mode) {
                    AuthMode.LOGIN -> {
                        vm.loginWithEmail(email, password) { success, result ->
                            loading = false
                            if (success) {
                                if (result == "SETUP_REQUIRED") onSetupRequired()
                                else onAuthSuccess()
                            } else {
                                error = result
                            }
                        }
                    }
                    AuthMode.REGISTER -> {
                        if (password != confirmPassword) {
                            loading = false
                            error = "Passwords do not match"
                            return@Button
                        }
                        vm.registerWithEmail(email, password) { success, result ->
                            loading = false
                            if (success) onSetupRequired()
                            else error = result
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = colors.onPrimary)
            else Text(
                text = when(mode) {
                    AuthMode.LOGIN -> "Login"
                    AuthMode.REGISTER -> "Register"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = {
            mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
            error = null
            message = null
        }) {
            Text(if (mode == AuthMode.LOGIN) "Don't have an account? Register" else "Already have an account? Login")
        }

        if (mode == AuthMode.REGISTER) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("By registering, you agree to our ", fontSize = 12.sp)
                TextButton(onClick = onTermsClick, contentPadding = PaddingValues(0.dp)) {
                    Text("Terms & Conditions", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
