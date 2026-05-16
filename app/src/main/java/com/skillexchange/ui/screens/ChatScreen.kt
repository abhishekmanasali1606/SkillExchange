package com.skillexchange.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.skillexchange.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: AppViewModel, swapId: String, onBack: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val messages by vm.messages.collectAsState()
    val allSwaps by vm.allSwaps.collectAsState()
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var msgText by remember { mutableStateOf("") }

    val swap = allSwaps.find { it.id == swapId }
    val uid = vm.currentUser?.id ?: ""
    val isOwner = swap?.postOwner == uid
    val otherId = if (isOwner) swap?.offeredBy else swap?.postOwner
    val otherUser = otherId?.let { vm.getUserById(it) }
    val myConfirmed = if (isOwner) swap?.confirmedByOwner == true else swap?.confirmedByOfferer == true

    LaunchedEffect(swapId) {
        vm.observeMessages(swapId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(32.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                Text((otherUser?.name ?: "?").first().uppercase(),
                                    color = colors.onPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                        Column {
                            Text(otherUser?.name ?: "Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(otherUser?.skillOffered ?: "", color = colors.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .navigationBarsPadding()
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = msgText,
                        onValueChange = { msgText = it },
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.weight(1f),
                        maxLines = 4
                    )
                    IconButton(
                        onClick = {
                            if (msgText.isNotBlank()) {
                                vm.sendMessage(swapId, msgText.trim())
                                msgText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                Modifier.padding(0.dp)
                            )
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (msgText.isNotBlank()) colors.primary else colors.surfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Send, null,
                                    tint = if (msgText.isNotBlank()) colors.onPrimary else colors.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Swap status banner
            Surface(color = colors.primaryContainer) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = colors.primary, modifier = Modifier.size(16.dp))
                    Text(
                        "${swap?.hoursOffered ?: 0}h swap • ${when (swap?.status) { "completed" -> "Completed"; "accepted" -> "Active"; else -> "Pending" }}",
                        color = colors.onPrimaryContainer, fontSize = 13.sp, modifier = Modifier.weight(1f)
                    )
                    if (swap?.status == "accepted" && !myConfirmed) {
                        TextButton(
                            onClick = { vm.confirmSwap(swapId) },
                            colors = ButtonDefaults.textButtonColors(contentColor = colors.primary)
                        ) {
                            Text("Mark Done", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Messages
            if (messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Chat, null, modifier = Modifier.size(40.dp), tint = colors.outline)
                        Spacer(Modifier.height(8.dp))
                        Text("Say hello!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Negotiate your skill swap.", color = colors.onSurfaceVariant)
                    }
                }
            } else {
                val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(messages, key = { it.id }) { msg ->
                        val isMe = msg.senderId == uid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isMe) {
                                Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text((otherUser?.name ?: "?").first().uppercase(),
                                            color = colors.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(Modifier.width(6.dp))
                            }
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 18.dp, topEnd = 18.dp,
                                    bottomStart = if (isMe) 18.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 18.dp
                                ),
                                color = if (isMe) colors.primary else colors.surface,
                                tonalElevation = if (isMe) 0.dp else 2.dp,
                                modifier = Modifier.widthIn(max = 280.dp)
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(msg.text, color = if (isMe) colors.onPrimary else colors.onSurface, fontSize = 15.sp)
                                    Text(fmt.format(Date(msg.timestamp)),
                                        color = if (isMe) colors.onPrimary.copy(alpha = 0.6f) else colors.onSurfaceVariant,
                                        fontSize = 11.sp, modifier = Modifier.align(Alignment.End))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
