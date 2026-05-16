package com.skillexchange.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skillexchange.data.model.User
import com.skillexchange.viewmodel.AppViewModel

private val Gold   = Color(0xFFFFD700)
private val Silver = Color(0xFFC0C0C0)
private val Bronze = Color(0xFFCD7F32)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(vm: AppViewModel, onBack: () -> Unit, onHome: () -> Unit, onSwaps: () -> Unit, onProfile: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    val users by vm.users.collectAsState()
    val currentUser = vm.currentUser

    val sorted = remember(users) {
        users.sortedWith(compareByDescending<User> { it.trustScore }.thenByDescending { it.points })
    }
    val myRank = sorted.indexOfFirst { it.id == currentUser?.id }.let { if (it < 0) null else it + 1 }
    val top3 = sorted.take(3)
    val rest = sorted.drop(3)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Community Leaders", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background)
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
                    selected = true,
                    onClick = { },
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
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Hero banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(listOf(colors.primary, colors.primaryContainer))
                        )
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EmojiEvents, null,
                            tint = Gold, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Skill Champions", color = colors.onPrimary,
                            fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Earn trust, earn respect.", color = colors.onPrimary.copy(0.8f), fontSize = 13.sp)
                    }
                }
            }

            // My rank card (if present)
            if (myRank != null && currentUser != null) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.secondaryContainer,
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.secondary,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("#$myRank", color = colors.onSecondary,
                                        fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Your Rank", color = colors.onSurfaceVariant, fontSize = 11.sp)
                                Text(currentUser.name, fontWeight = FontWeight.Bold, color = colors.onSecondaryContainer)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${currentUser.trustScore}/5", fontWeight = FontWeight.ExtraBold, color = colors.secondary)
                                    Text("Trust", color = colors.onSurfaceVariant, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${currentUser.points}", fontWeight = FontWeight.ExtraBold, color = colors.primary)
                                    Text("Points", color = colors.onSurfaceVariant, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Top 3 podium
            if (top3.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("TOP COMMUNITY MEMBERS", color = colors.onSurfaceVariant,
                            fontSize = 12.sp, fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Silver (2nd)
                        if (top3.size >= 2) {
                            PodiumCard(user = top3[1], rank = 2, medal = Silver,
                                modifier = Modifier.weight(1f).height(140.dp),
                                isCurrent = top3[1].id == currentUser?.id)
                        } else { Spacer(Modifier.weight(1f)) }

                        // Gold (1st)
                        PodiumCard(user = top3[0], rank = 1, medal = Gold,
                            modifier = Modifier.weight(1f).height(170.dp),
                            isCurrent = top3[0].id == currentUser?.id)

                        // Bronze (3rd)
                        if (top3.size >= 3) {
                            PodiumCard(user = top3[2], rank = 3, medal = Bronze,
                                modifier = Modifier.weight(1f).height(120.dp),
                                isCurrent = top3[2].id == currentUser?.id)
                        } else { Spacer(Modifier.weight(1f)) }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            // Rest of list
            if (rest.isNotEmpty()) {
                item {
                    Divider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.outline.copy(0.3f))
                    Spacer(Modifier.height(8.dp))
                }
                itemsIndexed(rest, key = { _, u -> u.id }) { index, user ->
                    LeaderRow(
                        user = user,
                        rank = index + 4,
                        isCurrent = user.id == currentUser?.id
                    )
                }
            }

            // Empty state
            if (sorted.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(48.dp), tint = colors.outline)
                            Spacer(Modifier.height(12.dp))
                            Text("No community members yet", fontWeight = FontWeight.Bold)
                            Text("Be the first to complete a swap!", color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PodiumCard(
    user: User,
    rank: Int,
    medal: Color,
    modifier: Modifier = Modifier,
    isCurrent: Boolean = false
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrent) colors.primaryContainer else colors.surface
        ),
        elevation = CardDefaults.cardElevation(if (rank == 1) 6.dp else 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.EmojiEvents, null,
                tint = medal, modifier = Modifier.size(if (rank == 1) 28.dp else 22.dp))
            Spacer(Modifier.height(4.dp))
            Surface(shape = CircleShape, color = medal.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.name.first().uppercase(), color = colors.onSurface,
                        fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(user.name.split(" ").first(), fontWeight = FontWeight.Bold,
                fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1)
            Text(user.skillOffered, color = colors.onSurfaceVariant,
                fontSize = 10.sp, textAlign = TextAlign.Center, maxLines = 1)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                Icon(Icons.Default.Star, null, tint = medal, modifier = Modifier.size(12.dp))
                Text("${user.trustScore}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = medal)
            }
            Text("${user.points} pts", color = colors.primary,
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun LeaderRow(user: User, rank: Int, isCurrent: Boolean) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isCurrent) colors.primaryContainer else colors.surface,
        tonalElevation = if (isCurrent) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("#$rank", color = colors.onSurfaceVariant, fontWeight = FontWeight.Bold,
                fontSize = 14.sp, modifier = Modifier.width(28.dp))
            Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(36.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(user.name.first().uppercase(), color = colors.onPrimary,
                        fontWeight = FontWeight.Bold)
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(user.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(user.skillOffered, color = colors.onSurfaceVariant, fontSize = 12.sp)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(5) { i ->
                    Icon(Icons.Default.Star, null,
                        tint = if (i < user.trustScore) Gold else colors.outline,
                        modifier = Modifier.size(13.dp))
                }
            }
            Text("${user.points}pts", color = colors.primary,
                fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}
