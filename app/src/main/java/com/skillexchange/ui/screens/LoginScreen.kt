package com.skillexchange.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

private val SKILLS = listOf(
    "Plumber", "Electrician", "Carpenter", "Mason", "Painter",
    "Welder", "Mechanic", "Roofer", "Farmer", "Cook", "Tailor", "Other"
)

@Composable
fun LoginScreen(vm: AppViewModel, onDone: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var skillOffered by remember { mutableStateOf("") }
    var skillWanted by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val colors = MaterialTheme.colorScheme

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 64.dp, bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Surface(
            shape = CircleShape,
            color = colors.primary,
            tonalElevation = 8.dp,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null,
                    tint = colors.onPrimary, modifier = Modifier.size(40.dp))
            }
        }
        Spacer(Modifier.height(16.dp))
        Text("Skill Exchange", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = colors.primary)
        Text("Trade your skills. Build your community.", color = colors.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        // Progress dots
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(4) { i ->
                Surface(
                    shape = RoundedCornerShape(50),
                    color = if (i <= step) colors.primary else colors.outline,
                    modifier = Modifier.height(8.dp).width(if (i == step) 28.dp else 8.dp)
                ) {}
            }
        }
        Spacer(Modifier.height(24.dp))

        // Card
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                when (step) {
                    0 -> {
                        Text("What's your name?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Your community will know you by this.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Your Name") },
                            placeholder = { Text("e.g. Ravi Kumar") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    1 -> {
                        Text("What skill do you offer?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Your expertise — what you help others with.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        SkillGrid(selected = skillOffered, onSelect = { skillOffered = it }, color = colors.primary)
                    }
                    2 -> {
                        Text("What skill do you need?", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("What would help you the most right now?", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(16.dp))
                        SkillGrid(
                            selected = skillWanted,
                            onSelect = { skillWanted = it },
                            color = colors.secondary,
                            exclude = skillOffered
                        )
                    }
                    3 -> {
                        Text("Contact & Location", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("Help others reach you easily.", color = colors.onSurfaceVariant, fontSize = 14.sp)
                        Spacer(Modifier.height(20.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("e.g. +91 9876543210") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = village,
                            onValueChange = { village = it },
                            label = { Text("Village / Locality") },
                            placeholder = { Text("e.g. Hampi") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
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
                    enabled = !loading
                ) {
                    if (loading) {
                        CircularProgressIndicator(
                            color = colors.onPrimary,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            if (step == 3) "Join Community" else "Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, contentDescription = null)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("1 hour of work = 1 Skill Point", color = colors.onSurfaceVariant, fontSize = 13.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillGrid(
    selected: String,
    onSelect: (String) -> Unit,
    color: androidx.compose.ui.graphics.Color,
    exclude: String = ""
) {
    val skills = listOf(
        "Plumber", "Electrician", "Carpenter", "Mason", "Painter",
        "Welder", "Mechanic", "Roofer", "Farmer", "Cook", "Tailor", "Other"
    ).filter { it != exclude }

    val colorScheme = MaterialTheme.colorScheme
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        skills.forEach { skill ->
            val isSelected = selected == skill
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(skill) },
                label = { Text(skill, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = color,
                    selectedLabelColor = colorScheme.onPrimary
                )
            )
        }
    }
}
