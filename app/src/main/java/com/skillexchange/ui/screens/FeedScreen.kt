package com.skillexchange.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.skillexchange.data.model.Post
import com.skillexchange.data.model.User
import com.skillexchange.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    vm: AppViewModel,
    onPostClick: (String) -> Unit,
    onLeaderboard: () -> Unit,
    onProfile: () -> Unit,
    onSwaps: () -> Unit,
    onVillage: () -> Unit,
    onTerms: () -> Unit,
    onLogout: () -> Unit
) {
    val posts by vm.posts.collectAsState()
    val successMsg by vm.successMessage.collectAsState()
    val isDarkMode by vm.isDarkMode.collectAsState()
    val colors = MaterialTheme.colorScheme
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(successMsg) {
        successMsg?.let {
            snackbarHostState.showSnackbar(it)
            vm.clearSuccess()
        }
    }

    val skillFilters = listOf("All", "Plumber", "Electrician", "Carpenter", "Mason",
        "Painter", "Welder", "Mechanic", "Roofer", "Farmer", "Cook", "Tailor", "Other")

    val filtered = posts.filter { post ->
        (filter == "All" || post.skillRequired == filter) &&
        (search.isBlank() || post.title.contains(search, true) || post.skillRequired.contains(search, true))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                    Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(64.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Text((vm.currentUser?.name ?: "U").first().uppercase(), 
                                color = colors.onPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(vm.currentUser?.name ?: "User", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(vm.currentUser?.skillOffered ?: "", color = colors.onSurfaceVariant, fontSize = 14.sp)
                }
                Divider()
                NavigationDrawerItem(
                    label = { Text("Home Board") },
                    selected = true,
                    onClick = { scope.launch { drawerState.close() } },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Our Village") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onVillage() },
                    icon = { Icon(Icons.Default.LocationCity, null) }
                )
                Divider()
                ListItem(
                    headlineContent = { Text("Dark Mode") },
                    trailingContent = {
                        Switch(checked = isDarkMode, onCheckedChange = { vm.toggleTheme() })
                    },
                    leadingContent = {
                        Icon(if (isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode, null)
                    }
                )
                NavigationDrawerItem(
                    label = { Text("Terms & Rules") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onTerms() },
                    icon = { Icon(Icons.Default.Gavel, null) }
                )
                NavigationDrawerItem(
                    label = { Text("Logout") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onLogout() },
                    icon = { Icon(Icons.Default.Logout, null) }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Skill Board", fontWeight = FontWeight.ExtraBold) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, null)
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { onPostClick("NEW_POST") },
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("Post Need") },
                    containerColor = colors.primary,
                    contentColor = colors.onPrimary
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = true,
                        onClick = { },
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
                        selected = false,
                        onClick = onProfile,
                        icon = { Icon(Icons.Default.Person, "Profile") },
                        label = { Text("Profile") }
                    )
                }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                // Header Search
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    OutlinedTextField(
                        value = search,
                        onValueChange = { search = it },
                        placeholder = { Text("Search skills...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                // Skill filter chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(skillFilters) { skill ->
                        FilterChip(
                            selected = filter == skill,
                            onClick = { filter = skill },
                            label = { Text(skill) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = colors.primary,
                                selectedLabelColor = colors.onPrimary
                            )
                        )
                    }
                }

                // Posts
                if (filtered.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No posts found", color = colors.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filtered, key = { it.id }) { post ->
                            PostCard(
                                post = post,
                                author = vm.getUserById(post.userId),
                                onClick = { onPostClick(post.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PostCard(post: Post, author: User?, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Surface(shape = RoundedCornerShape(8.dp), color = colors.secondaryContainer) {
                    Text(post.skillRequired, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), 
                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
                Text(timeAgo(post.timestamp), color = colors.onSurfaceVariant, fontSize = 11.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.AccessTime, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text("${post.hoursRequired}h required", color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.LocationOn, null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    Text(post.village, color = colors.onSurfaceVariant, fontSize = 12.sp)
                }
            }
            Text(post.description, color = colors.onSurfaceVariant, fontSize = 14.sp, maxLines = 2)
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = CircleShape, color = colors.primary, modifier = Modifier.size(24.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Text((author?.name ?: "U").first().uppercase(), color = colors.onPrimary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(author?.name ?: "User", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.weight(1f))
                StarRow(count = author?.trustScore ?: 0)
            }
        }
    }
}

@Composable
fun StarRow(count: Int) {
    Row {
        repeat(5) { i ->
            Icon(Icons.Default.Star, null, 
                tint = if (i < count) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(12.dp))
        }
    }
}

fun timeAgo(ts: Long): String {
    val diff = System.currentTimeMillis() - ts
    val mins = diff / 60000
    if (mins < 60) return "${mins}m ago"
    val hrs = mins / 60
    if (hrs < 24) return "${hrs}h ago"
    return "${hrs / 24}d ago"
}
