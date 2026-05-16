package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: AppViewModel, onLogout: () -> Unit, onHome: () -> Unit, onSwaps: () -> Unit, onLeaderboard: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val allSwaps by vm.allSwaps.collectAsState()
    val mySwaps = remember(allSwaps) { vm.mySwaps() }
    val completed = mySwaps.count { it.status == "completed" }
    val trust     = vm.currentUser?.trustScore ?: 0

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // Edit Profile State
    var editSkillOffered by remember { mutableStateOf(vm.currentUser?.skillOffered ?: "") }
    var editSkillWanted by remember { mutableStateOf(vm.currentUser?.skillWanted ?: "") }
    var editPhone by remember { mutableStateOf(vm.currentUser?.phoneNumber ?: "") }
    var editVillage by remember { mutableStateOf(vm.currentUser?.village ?: "") }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profile") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone Number") })
                    OutlinedTextField(value = editVillage, onValueChange = { editVillage = it }, label = { Text("Village / Locality") })
                    OutlinedTextField(value = editSkillOffered, onValueChange = { editSkillOffered = it }, label = { Text("Skill Offered") })
                    OutlinedTextField(value = editSkillWanted, onValueChange = { editSkillWanted = it }, label = { Text("Skill Wanted") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.updateUserProfile(editSkillOffered, editSkillWanted, editPhone, editVillage)
                    showEditDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Account?") },
            text = { Text("This action cannot be undone. All your skill points and trust score will be permanently lost.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        loading = true
                        vm.deleteAccount { success, error ->
                            loading = false
                            if (success) onLogout()
                        }
                    },
                    enabled = !loading
                ) {
                    Text("Delete", color = colors.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile") },
                actions = {
                    IconButton(onClick = { 
                        editSkillOffered = vm.currentUser?.skillOffered ?: ""
                        editSkillWanted = vm.currentUser?.skillWanted ?: ""
                        editPhone = vm.currentUser?.phoneNumber ?: ""
                        editVillage = vm.currentUser?.village ?: ""
                        showEditDialog = true 
                    }) {
                        Icon(Icons.Default.Edit, "Edit Profile")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onSwaps,
                    icon = { Icon(Icons.Default.SwapHoriz, "Swaps") },
                    label = { Text("Swaps") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onLeaderboard,
                    icon = { Icon(Icons.Default.EmojiEvents, "Leaderboard") },
                    label = { Text("Rank") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Info
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(80.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((vm.currentUser?.name ?: "U").first().uppercase(), 
                            color = colors.onPrimary, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Column {
                    Text(vm.currentUser?.name ?: "User", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(vm.currentUser?.village?.ifBlank { "Location not set" } ?: "Location not set", color = colors.onSurfaceVariant, fontSize = 14.sp)
                }
            }

            // Stats
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "${vm.currentUser?.points ?: 0}", "Skill Points", Icons.Default.ElectricBolt)
                StatCard(Modifier.weight(1f), "$completed", "Total Swaps", Icons.Default.SwapHoriz)
                StatCard(Modifier.weight(1f), "$trust/5", "Trust Score", Icons.Default.Shield)
            }

            Divider()

            // User's Posts
            val allPosts by vm.posts.collectAsState()
            val myPosts = remember(allPosts) { allPosts.filter { it.userId == vm.currentUser?.id } }

            Text("My Posts (${myPosts.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            if (myPosts.isEmpty()) {
                Text("You haven't posted any needs yet.", color = colors.onSurfaceVariant, fontSize = 14.sp)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    myPosts.forEach { post ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colors.surfaceVariant.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(post.title, fontWeight = FontWeight.Bold)
                                Text(post.description, color = colors.onSurfaceVariant, fontSize = 13.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }

            Divider()

            // Profile Details
            Text("Profile Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    DetailRow(Icons.Default.Phone, "Phone Number", vm.currentUser?.phoneNumber ?: "Not set", colors.primary)
                    DetailRow(Icons.Default.LocationOn, "Village / Locality", vm.currentUser?.village ?: "Not set", colors.primary)
                    DetailRow(Icons.Default.Build, "Skill Offered", vm.currentUser?.skillOffered ?: "-", colors.primary)
                    DetailRow(Icons.Default.Search, "Skill Wanted", vm.currentUser?.skillWanted ?: "-", colors.secondary)
                }
            }

            // Time Commitment
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = colors.primaryContainer.copy(alpha = 0.3f))) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Timer, null, tint = colors.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Exchange Rule", fontWeight = FontWeight.Bold)
                        Text("1 Hour of work required per Skill Point earned.", fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            
            // Danger Zone
            Text("Danger Zone", fontWeight = FontWeight.Bold, color = colors.error, fontSize = 16.sp)
            
            OutlinedButton(
                onClick = { 
                    loading = true
                    vm.deleteAllPosts { loading = false }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.error),
                enabled = !loading
            ) {
                Icon(Icons.Default.ClearAll, null)
                Spacer(Modifier.width(8.dp))
                Text("Delete All Posts (App Wide)")
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.error),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.error),
                enabled = !loading
            ) {
                Icon(Icons.Default.DeleteForever, null)
                Spacer(Modifier.width(8.dp))
                Text("Delete My Account")
            }
        }
    }
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, iconColor: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, value: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Text(label, fontSize = 10.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}
