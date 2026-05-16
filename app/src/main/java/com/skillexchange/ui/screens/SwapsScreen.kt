package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.data.model.Swap
import com.skillexchange.viewmodel.AppViewModel

@Composable
fun SwapsScreen(vm: AppViewModel, onChat: (String) -> Unit, onHome: () -> Unit, onLeaderboard: () -> Unit, onProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val allSwaps by vm.allSwaps.collectAsState()
    val successMsg by vm.successMessage.collectAsState()
    val mySwaps = remember(allSwaps) { vm.mySwaps() }
    
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(successMsg) {
        successMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    val pending   = mySwaps.filter { it.status == "pending" }
    val active    = mySwaps.filter { it.status == "accepted" }
    val completed = mySwaps.filter { it.status == "completed" }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = onHome,
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = true,
                    onClick = { },
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
                    selected = false,
                    onClick = onProfile,
                    icon = { Icon(Icons.Default.Person, "Profile") },
                    label = { Text("Profile") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp)) {
                Text("My Swaps", fontWeight = FontWeight.ExtraBold, fontSize = 26.sp)
                Text("${mySwaps.size} skill trades", color = colors.onSurfaceVariant, fontSize = 14.sp)
            }

            if (mySwaps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(48.dp), tint = colors.outline)
                        Spacer(Modifier.height(12.dp))
                        Text("No swaps yet", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Browse the Skill Board and make an offer!", color = colors.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (pending.isNotEmpty()) {
                        item { SectionHeader("Incoming Offers", Icons.Default.Notifications, pending.size) }
                        items(pending, key = { it.id }) { swap ->
                            SwapCard(swap = swap, vm = vm, onChat = onChat)
                        }
                    }
                    if (active.isNotEmpty()) {
                        item { SectionHeader("Active Swaps", Icons.Default.ElectricBolt, active.size) }
                        items(active, key = { it.id }) { swap ->
                            SwapCard(swap = swap, vm = vm, onChat = onChat)
                        }
                    }
                    if (completed.isNotEmpty()) {
                        item { SectionHeader("Completed", Icons.Default.CheckCircle, completed.size) }
                        items(completed, key = { it.id }) { swap ->
                            SwapCard(swap = swap, vm = vm, onChat = onChat)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, count: Int) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = colors.onSurfaceVariant)
        Text(title.uppercase(), color = colors.onSurfaceVariant, fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold, letterSpacing = 0.5.sp)
        Surface(shape = RoundedCornerShape(12.dp), color = colors.primaryContainer) {
            Text("$count", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                color = colors.onPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SwapCard(swap: Swap, vm: AppViewModel, onChat: (String) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val uid = vm.currentUser?.id ?: ""
    val isOwner = swap.postOwner == uid
    val otherId = if (isOwner) swap.offeredBy else swap.postOwner
    val otherUser = vm.getUserById(otherId)
    val myConfirmed = if (isOwner) swap.confirmedByOwner else swap.confirmedByOfferer

    val statusColor = when (swap.status) {
        "completed" -> colors.primary
        "accepted"  -> colors.secondary
        else        -> colors.onSurfaceVariant
    }
    val statusLabel = when {
        swap.status == "completed" -> "Completed"
        swap.status == "accepted"  -> "Active"
        isOwner -> "New Offer"
        else    -> "Awaiting Response"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(40.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((otherUser?.name ?: "?").first().uppercase(), color = colors.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(otherUser?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                    Text(otherUser?.skillOffered ?: "", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
                Surface(shape = RoundedCornerShape(20.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(statusLabel, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                }
            }

            Surface(shape = RoundedCornerShape(8.dp), color = colors.primaryContainer) {
                Row(modifier = Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AccessTime, null, tint = colors.onPrimaryContainer, modifier = Modifier.size(16.dp))
                    Text("${swap.hoursOffered} hour${if (swap.hoursOffered != 1) "s" else ""} offered",
                        color = colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }

            if (swap.message.isNotBlank()) {
                Text("\"${swap.message}\"", color = colors.onSurfaceVariant, fontSize = 13.sp, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (swap.status == "pending" && isOwner) {
                    Button(onClick = { vm.acceptSwap(swap.id) }, shape = RoundedCornerShape(20.dp)) {
                        Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Accept", fontWeight = FontWeight.Bold)
                    }
                }
                if (swap.status == "accepted") {
                    Button(
                        onClick = { onChat(swap.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.secondary),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Chat", fontWeight = FontWeight.Bold)
                    }
                    if (!myConfirmed) {
                        OutlinedButton(onClick = { vm.confirmSwap(swap.id) }, shape = RoundedCornerShape(20.dp)) {
                            Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Mark Done", fontWeight = FontWeight.Bold)
                        }
                    }
                }
                if (swap.status == "completed") {
                    Surface(shape = RoundedCornerShape(20.dp), color = colors.primaryContainer) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Text("Trust score updated!", color = colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
