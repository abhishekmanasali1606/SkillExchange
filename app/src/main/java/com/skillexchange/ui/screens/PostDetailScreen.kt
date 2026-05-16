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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(vm: AppViewModel, postId: String, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val post = vm.getPostById(postId)
    val author = post?.let { vm.getUserById(it.userId) }
    val allSwaps by vm.allSwaps.collectAsState()

    var hours by remember { mutableIntStateOf(1) }
    var message by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var sent by remember { mutableStateOf(false) }

    val isOwner = post?.userId == vm.currentUser?.id
    val alreadyOffered = allSwaps.any { it.postId == postId && it.offeredBy == vm.currentUser?.id }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Post Detail", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        }
    ) { padding ->
        if (post == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Post not found.")
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Post card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(shape = RoundedCornerShape(20.dp), color = colors.primaryContainer) {
                        Text(
                            "Needs: ${post.skillRequired}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            color = colors.onPrimaryContainer, fontWeight = FontWeight.Bold, fontSize = 13.sp
                        )
                    }
                    Text(post.title, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Timer, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Text("${post.hoursRequired}h required", color = colors.onSurfaceVariant, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.LocationOn, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(16.dp))
                            Text(post.village, color = colors.onSurfaceVariant, fontSize = 13.sp)
                        }
                    }
                    Text(post.description, color = colors.onSurfaceVariant, fontSize = 15.sp)
                    Divider(color = colors.outline.copy(alpha = 0.4f))
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(44.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text((author?.name ?: "?").first().uppercase(), color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(author?.name ?: "Unknown", fontWeight = FontWeight.Bold)
                            Text("Offers: ${author?.skillOffered ?: "-"}", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        }
                        StarRow(count = author?.trustScore ?: 0)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.ElectricBolt, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                            Text("${author?.points ?: 0} pts", fontWeight = FontWeight.SemiBold)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Icon(Icons.Default.Shield, null, tint = colors.secondary, modifier = Modifier.size(16.dp))
                            Text("Trust ${author?.trustScore ?: 0}/5", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Swap offer form
            if (!isOwner && !alreadyOffered && !sent) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Make a Swap Offer", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        Text(
                            "Offer your ${vm.currentUser?.skillOffered} skill in exchange for their help.",
                            color = colors.onSurfaceVariant, fontSize = 14.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hours you'll offer:", fontWeight = FontWeight.Bold)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { if (hours > 1) hours-- }) {
                                    Icon(Icons.Default.Remove, null)
                                }
                                Text("$hours", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp,
                                    color = colors.primary, modifier = Modifier.widthIn(min = 32.dp))
                                IconButton(onClick = { if (hours < 24) hours++ }) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        }
                        OutlinedTextField(
                            value = message,
                            onValueChange = { message = it },
                            label = { Text("Message (optional)") },
                            placeholder = { Text("Tell them about your experience...") },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp),
                            maxLines = 4
                        )
                        Surface(shape = RoundedCornerShape(10.dp), color = colors.primaryContainer) {
                            Row(modifier = Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Info, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                                Text(
                                    "Completing this swap earns both of you $hours Skill Point${if (hours > 1) "s" else ""} and increases Trust Score.",
                                    color = colors.onPrimaryContainer, fontSize = 13.sp
                                )
                            }
                        }
                        Button(
                            onClick = {
                                loading = true
                                vm.createSwap(post.id, post.userId, hours, message.trim()) {
                                    loading = false
                                    sent = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !loading
                        ) {
                            if (loading) {
                                CircularProgressIndicator(color = colors.onPrimary, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.SwapHoriz, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Send Swap Offer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }

            if (sent || alreadyOffered) {
                Surface(shape = RoundedCornerShape(16.dp), color = colors.primaryContainer) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = colors.primary)
                        Text("Swap offer sent! Check 'My Swaps' to track it.", color = colors.onPrimaryContainer, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isOwner) {
                Surface(shape = RoundedCornerShape(16.dp), color = colors.surfaceVariant) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, tint = colors.onSurfaceVariant)
                        Text("This is your post. Check 'My Swaps' for incoming offers.", color = colors.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
