package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileSetupScreen(vm: AppViewModel, onDone: () -> Unit) {
    val colors = MaterialTheme.colorScheme

    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var skillOffered by remember { mutableStateOf("") }
    var skillWanted by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val skills = listOf(
        "Plumber", "Electrician", "Carpenter", "Mason", "Painter",
        "Welder", "Mechanic", "Roofer", "Farmer", "Cook", "Tailor", "Other"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = colors.secondary,
            modifier = Modifier.size(72.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = colors.onSecondary, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Set Up Your Profile", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
        Text(
            "Almost there! Now tell us about your skills.",
            color = colors.onSurfaceVariant, fontSize = 14.sp
        )
        Spacer(Modifier.height(28.dp))

        // Progress
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (i <= step) colors.secondary else colors.outline,
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
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (step) {
                    0 -> {
                        Text("What's your name?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Your community will know you by this name.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Ravi Kumar") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                    1 -> {
                        Text("What skill do you offer?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Your expertise — what you help others with.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            skills.forEach { skill ->
                                FilterChip(
                                    selected = skillOffered == skill,
                                    onClick = { skillOffered = skill },
                                    label = { Text(skill, fontWeight = if (skillOffered == skill) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.primary,
                                        selectedLabelColor = colors.onPrimary
                                    )
                                )
                            }
                        }
                    }
                    2 -> {
                        Text("What skill do you need?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("What help would benefit you the most?", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            skills.filter { it != skillOffered }.forEach { skill ->
                                FilterChip(
                                    selected = skillWanted == skill,
                                    onClick = { skillWanted = skill },
                                    label = { Text(skill, fontWeight = if (skillWanted == skill) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = colors.secondary,
                                        selectedLabelColor = colors.onSecondary
                                    )
                                )
                            }
                        }
                    }
                    3 -> {
                        Text("Contact & Location", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Help others reach you easily.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("e.g. +91 9876543210") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village / Locality") },
                            placeholder = { Text("e.g. Hampi") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        when (step) {
                            0 -> if (name.isNotBlank()) step = 1
                            1 -> if (skillOffered.isNotEmpty()) step = 2
                            2 -> if (skillWanted.isNotEmpty()) step = 3
                            3 -> if (phoneNumber.isNotBlank() && village.isNotBlank()) {
                                loading = true
                                vm.setupProfile(name.trim(), skillOffered, skillWanted, phoneNumber.trim(), village.trim()) {
                                    loading = false
                                    onDone()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.secondary)
                ) {
                    if (loading) {
                        CircularProgressIndicator(color = colors.onSecondary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (step == 3) "Join Community" else "Continue",
                            fontWeight = FontWeight.Bold, fontSize = 16.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }
                }
            }
        }
    }
}
