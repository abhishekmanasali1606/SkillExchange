package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PostScreen(vm: AppViewModel, onDone: () -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var skillRequired by remember { mutableStateOf("") }
    var hoursRequired by remember { mutableStateOf("1") }
    var village by remember { mutableStateOf(vm.currentUser?.village ?: "") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    val colors = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()

    val skills = listOf("Plumber", "Electrician", "Carpenter", "Mason", "Painter",
        "Welder", "Mechanic", "Roofer", "Farmer", "Cook", "Tailor", "Other")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 48.dp)
    ) {
        Text("Post a Need", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
        Text("Ask your community for help", color = colors.onSurfaceVariant, fontSize = 14.sp)
        Spacer(Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("What do you need?", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Leaking roof needs fixing") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Village / Locality", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = village,
                    onValueChange = { village = it },
                    placeholder = { Text("e.g. Hampi") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Estimated Time Required (Hours)", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = hoursRequired,
                    onValueChange = { if (it.all { char -> char.isDigit() }) hoursRequired = it },
                    placeholder = { Text("e.g. 2") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Text("Skill Required", fontWeight = FontWeight.Bold)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    skills.forEach { skill ->
                        FilterChip(
                            selected = skillRequired == skill,
                            onClick = { skillRequired = skill },
                            label = { Text(skill) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            )
                        )
                    }
                }

                Text("Describe the Problem", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = { Text("Explain what help you need and what you'll offer in return...") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp),
                    maxLines = 6
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Offer hint
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = colors.primaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = colors.primary)
                Column {
                    Text("You offer: ${vm.currentUser?.skillOffered}", fontWeight = FontWeight.Bold,
                        color = colors.primary, fontSize = 14.sp)
                    Text("Others can swap their skill in exchange for your help.",
                        color = colors.onPrimaryContainer, fontSize = 13.sp)
                }
            }
        }

        if (error.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            Text(error, color = colors.error, fontSize = 13.sp)
        }

        Spacer(Modifier.height(20.dp))
        Button(
            onClick = {
                error = ""
                val hrs = hoursRequired.toIntOrNull() ?: 0
                when {
                    title.isBlank() -> error = "Please enter a title."
                    village.isBlank() -> error = "Please enter your village."
                    hrs <= 0 -> error = "Please enter valid hours."
                    skillRequired.isEmpty() -> error = "Please select the skill you need."
                    description.isBlank() -> error = "Please describe your need."
                    else -> {
                        loading = true
                        vm.createPost(title.trim(), description.trim(), skillRequired, hrs, village.trim()) {
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
                CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Post Need", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
