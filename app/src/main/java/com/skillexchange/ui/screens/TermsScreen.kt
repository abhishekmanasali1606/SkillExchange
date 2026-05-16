package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Terms & Conditions") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            Text("Skill Exchange Rules", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            
            TermItem(
                title = "1. Skill Currency",
                description = "The app uses a 1:1 time-based exchange. One hour of service provided equals one Skill Point earned."
            )
            
            TermItem(
                title = "2. Community Trust",
                description = "Users must be honest about their skills. Misrepresenting expertise may lead to account suspension."
            )
            
            TermItem(
                title = "3. Respect & Safety",
                description = "Always meet in safe, public places for the first exchange. Treat all community members with respect."
            )
            
            TermItem(
                title = "4. Verification",
                description = "Confirm swaps only after the work is completed. False confirmations will result in point deduction."
            )

            TermItem(
                title = "5. Village Participation",
                description = "Users are encouraged to participate in village-level skill improvements to boost their community trust score."
            )

            Spacer(Modifier.height(24.dp))
            Text(
                "By using this app, you agree to these rules and our privacy policy regarding the collection of your skill data to help match you with others in your village.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TermItem(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = 20.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
        Spacer(Modifier.height(4.dp))
        Text(description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
