package com.fedeveloper95.games

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fedeveloper95.games.elements.MainActivity.AddAppsBottomSheet
import com.fedeveloper95.games.elements.MainActivity.CommunityBottomSheet
import com.fedeveloper95.games.elements.MainActivity.DeletePopup
import com.fedeveloper95.games.elements.MainActivity.Edit.EditAppBottomSheet
import com.fedeveloper95.games.elements.MainActivity.GameListItem
import com.fedeveloper95.games.elements.MainActivity.GridGameCard
import com.fedeveloper95.games.elements.MainActivity.HorizontalGamePager
import com.fedeveloper95.games.elements.MainActivity.MoreBottomSheet
import com.fedeveloper95.games.elements.ui.EmptyState
import com.fedeveloper95.games.elements.ui.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GetMoreGamesCard
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.elements.ui.HomeSearchBar
import com.fedeveloper95.games.services.SettingsActivity.Updater
import com.fedeveloper95.games.services.ShortcutHelper
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.GameViewModel
import com.fedeveloper95.games.services.mainactivity.GamesCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val pkgToLaunch = intent?.getStringExtra("LAUNCH_PKG")
        if (!pkgToLaunch.isNullOrEmpty()) {
            val launchIntent = packageManager.getLaunchIntentForPackage(pkgToLaunch)
            if (launchIntent != null) {
                startActivity(launchIntent)
            }
        }

        setContent {
            GameHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameHubScreen()
                }
            }
        }
    }
}

enum class ViewType { Pager, Grid, List }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalTextApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalLayoutApi::class)
@Composable
fun GameHubScreen(viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isExpandedScreen = configuration.screenWidthDp >= 600

    val vmGames by viewModel.games.collectAsState()
    var cachedGames by remember { mutableStateOf<List<GameApp>>(emptyList()) }
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val loaded = GamesCacheManager.loadGames(context)
            if (loaded.isNotEmpty()) {
                cachedGames = loaded
            }
        }
    }

    val games = if (vmGames.isEmpty() && cachedGames.isNotEmpty()) cachedGames else vmGames

    var isInitialLoad by remember { mutableStateOf(cachedGames.isEmpty() && vmGames.isEmpty()) }

    LaunchedEffect(isLoading, games) {
        if (!isLoading || games.isNotEmpty()) {
            isInitialLoad = false
        }
    }

    LaunchedEffect(vmGames) {
        if (vmGames.isNotEmpty()) {
            launch(Dispatchers.IO) {
                GamesCacheManager.saveGames(context, vmGames)
            }
        }
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var showCommunitySheet by remember { mutableStateOf(false) }
    var moreBottomSheetGame by remember { mutableStateOf<GameApp?>(null) }
    var gameToRemove by remember { mutableStateOf<GameApp?>(null) }
    var gameToEdit by remember { mutableStateOf<GameApp?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedGamePkg by remember { mutableStateOf<String?>(null) }

    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }
    val currentCardStyle = remember { mutableStateOf(prefs.getString("pref_card_style", "Default") ?: "Default") }
    val gridColumns = remember { mutableIntStateOf(prefs.getInt("pref_grid_columns", 2)) }
    val showGetMoreGames = remember { mutableStateOf(prefs.getBoolean("pref_show_get_more_games", true)) }
    val autoUpdates = remember { mutableStateOf(prefs.getBoolean("pref_auto_updates", true)) }
    val showUserName = remember { mutableStateOf(prefs.getBoolean("pref_show_user_name", true)) }
    val userName = remember { mutableStateOf(prefs.getString("pref_user_name", "User") ?: "User") }
    val sortType = remember { mutableStateOf(prefs.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical") }
    val statsInterval = remember { mutableFloatStateOf(prefs.getFloat("pref_stats_interval", 3f)) }

    val scope = rememberCoroutineScope()
    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { }

    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = R.font.sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.slant(-9f),
                FontVariation.width(111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    LaunchedEffect(currentVersionName) {
        val lastVersion = prefs.getString("last_version", null)
        if (lastVersion != currentVersionName) {
            showCommunitySheet = true
        }
    }

    LaunchedEffect(games) {
        ShortcutHelper.updateDynamicShortcuts(context, games)
    }

    LaunchedEffect(Unit) {
        if (autoUpdates.value) {
            if (Build.VERSION.SDK_INT >= 33) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            val update = Updater.checkForUpdates(currentVersionName)
            if (update != null) {
                Updater.showUpdateNotification(context, update)
            }
        }
    }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "pref_card_style") {
                currentCardStyle.value = sharedPreferences.getString("pref_card_style", "Default") ?: "Default"
            }
            if (key == "pref_grid_columns") {
                gridColumns.intValue = sharedPreferences.getInt("pref_grid_columns", 2)
            }
            if (key == "pref_show_get_more_games") {
                showGetMoreGames.value = sharedPreferences.getBoolean("pref_show_get_more_games", true)
            }
            if (key == "pref_auto_updates") {
                autoUpdates.value = sharedPreferences.getBoolean("pref_auto_updates", true)
            }
            if (key == "pref_show_user_name") {
                showUserName.value = sharedPreferences.getBoolean("pref_show_user_name", true)
            }
            if (key == "pref_user_name") {
                userName.value = sharedPreferences.getString("pref_user_name", "User") ?: "User"
            }
            if (key == "pref_sort_type") {
                sortType.value = sharedPreferences.getString("pref_sort_type", "Alphabetical") ?: "Alphabetical"
                viewModel.loadGames(context)
            }
            if (key == "pref_stats_interval") {
                statsInterval.floatValue = sharedPreferences.getFloat("pref_stats_interval", 3f)
                viewModel.loadGames(context)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadGames(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val displayGames = remember(games, searchQuery) {
        if (searchQuery.isBlank()) games
        else {
            val query = searchQuery.trim()
            games.filter {
                it.name.contains(query, ignoreCase = true) ||
                        (it.customName != null && it.customName.contains(query, ignoreCase = true))
            }
        }
    }

    val favoriteGames = remember(displayGames) { displayGames.filter { it.isFavorite } }
    val normalGames = remember(displayGames) { displayGames.filter { !it.isFavorite } }

    val openPlayStore: (String) -> Unit = { packageName ->
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    val launchGame: (GameApp) -> Unit = { game ->
        if (expandedGamePkg != null) {
            expandedGamePkg = null
        } else {
            game.launchIntent?.let {
                viewModel.incrementLaunchCount(context, game.packageName)
                context.startActivity(it)
                if (prefs.getBoolean("pref_bubble_enabled", false) && android.provider.Settings.canDrawOverlays(context)) {
                    context.startService(Intent(context, com.fedeveloper95.games.elements.GameBubble.GameBubbleService::class.java))
                }
            }
        }
    }

    val currentViewType = remember(currentCardStyle.value) {
        when (currentCardStyle.value) {
            "Horizontal" -> ViewType.Pager
            "Grid" -> ViewType.Grid
            else -> ViewType.List
        }
    }

    val longClickGame: (GameApp) -> Unit = { game ->
        moreBottomSheetGame = game
    }

    val moveGameUp: (GameApp) -> Unit = { game ->
        val currentList = viewModel.games.value.toMutableList()
        val index = currentList.indexOfFirst { it.packageName == game.packageName }
        if (index > 0) {
            val temp = currentList[index - 1]
            currentList[index - 1] = currentList[index]
            currentList[index] = temp
            viewModel.updateGamesOrder(currentList)
            if (sortType.value != "Custom") {
                viewModel.switchToCustomSort(context)
                sortType.value = "Custom"
                prefs.edit().putString("pref_sort_type", "Custom").apply()
            }
            viewModel.saveOrder(context)
        }
    }

    val moveGameDown: (GameApp) -> Unit = { game ->
        val currentList = viewModel.games.value.toMutableList()
        val index = currentList.indexOfFirst { it.packageName == game.packageName }
        if (index != -1 && index < currentList.size - 1) {
            val temp = currentList[index + 1]
            currentList[index + 1] = currentList[index]
            currentList[index] = temp
            viewModel.updateGamesOrder(currentList)
            if (sortType.value != "Custom") {
                viewModel.switchToCustomSort(context)
                sortType.value = "Custom"
                prefs.edit().putString("pref_sort_type", "Custom").apply()
            }
            viewModel.saveOrder(context)
        }
    }

    val listState = rememberLazyListState()
    val gridState = rememberLazyGridState()

    BackHandler(enabled = searchQuery.isNotEmpty()) {
        if (searchQuery.isNotEmpty()) {
            searchQuery = ""
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButtonMenu(
                expanded = false,
                button = {
                    ToggleFloatingActionButton(
                        checked = false,
                        onCheckedChange = { showAddSheet = true },
                        modifier = Modifier.animateFloatingActionButton(
                            visible = searchQuery.isEmpty(),
                            alignment = Alignment.BottomEnd
                        )
                    ) {
                        val imageVector by remember { derivedStateOf { if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add } }
                        Icon(
                            painter = rememberVectorPainter(imageVector),
                            contentDescription = stringResource(R.string.add_game),
                            modifier = Modifier.animateIcon({ checkedProgress })
                        )
                    }
                }
            ) {}
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
                .then(if (isExpandedScreen) Modifier.padding(horizontal = 64.dp) else Modifier)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 16.dp)
            ) {
                val buttonsContent = @Composable {
                    ExpressiveIconButton(
                        onClick = {
                            val intent = Intent(context, SettingsActivity::class.java)
                            context.startActivity(intent)
                        },
                        icon = Icons.Default.Settings,
                        contentDescription = stringResource(R.string.settings_title),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (showUserName.value) {
                            Text(
                                text = "${userName.value}'s",
                                style = TextStyle(
                                    fontFamily = customWelcomeFontFamily,
                                    fontSize = 28.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = stringResource(R.string.app_name),
                            fontFamily = GoogleSansFlex,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = if (showUserName.value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            lineHeight = 48.sp,
                            modifier = Modifier.offset(y = (-4).dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    buttonsContent()
                }
            }

            AnimatedVisibility(
                visible = true,
                enter = expandVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)) + fadeIn(),
                exit = shrinkVertically(spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessLow)) + fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HomeSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                    val spacing = 12.dp
                    Spacer(modifier = Modifier.height(spacing))
                    Text(
                        text = "${displayGames.size} ${stringResource(R.string.games_count_suffix)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(start = 4.dp),
                        fontFamily = GoogleSansFlex
                    )
                    Spacer(modifier = Modifier.height(spacing))
                }
            }

            Box(modifier = Modifier
                .weight(1f)
                .pointerInput(expandedGamePkg) {
                    detectTapGestures(
                        onTap = {
                            if (expandedGamePkg != null) {
                                expandedGamePkg = null
                            }
                        }
                    )
                }
            ) {
                if (isInitialLoad && isLoading && games.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(modifier = Modifier.size(64.dp))
                    }
                } else {
                    var isRefreshing by remember { mutableStateOf(false) }
                    val pullRefreshState = rememberPullToRefreshState()

                    val onRefresh: () -> Unit = {
                        isRefreshing = true
                        scope.launch {
                            viewModel.loadGames(context)
                            delay(1000)
                            isRefreshing = false
                        }
                    }

                    PullToRefreshBox(
                        state = pullRefreshState,
                        isRefreshing = isRefreshing,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = pullRefreshState,
                                isRefreshing = isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
                        if (games.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState()),
                                contentAlignment = Alignment.Center
                            ) {
                                EmptyState()
                            }
                        } else {
                            AnimatedContent(
                                targetState = currentViewType,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.95f, animationSpec = tween(200)) togetherWith
                                            fadeOut(animationSpec = tween(200))
                                },
                                label = "mainContentAnim"
                            ) { viewType ->
                                when (viewType) {
                                    ViewType.Pager -> {
                                        HorizontalGamePager(
                                            games = displayGames,
                                            onLaunch = launchGame,
                                            onLongClick = longClickGame
                                        )
                                    }
                                    ViewType.Grid -> {
                                        LazyVerticalGrid(
                                            state = gridState,
                                            columns = if (isExpandedScreen) GridCells.Adaptive(160.dp) else GridCells.Fixed(gridColumns.intValue),
                                            contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                            verticalArrangement = Arrangement.spacedBy(16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    Card(
                                                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                                        shape = RoundedCornerShape(28.dp),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                        ),
                                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                    ) {
                                                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                            Text(
                                                                text = stringResource(R.string.favorites),
                                                                style = MaterialTheme.typography.titleMedium,
                                                                color = MaterialTheme.colorScheme.primary,
                                                                fontFamily = GoogleSansFlex,
                                                                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                                                            )

                                                            val availableWidth = configuration.screenWidthDp - if (isExpandedScreen) 168 else 40
                                                            val cols = if (isExpandedScreen) maxOf(2, availableWidth / 160) else gridColumns.intValue
                                                            val chunkedFavorites = favoriteGames.chunked(cols)

                                                            Column(
                                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                                verticalArrangement = Arrangement.spacedBy(16.dp)
                                                            ) {
                                                                chunkedFavorites.forEach { rowGames ->
                                                                    Row(
                                                                        modifier = Modifier.fillMaxWidth(),
                                                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                                    ) {
                                                                        rowGames.forEach { game ->
                                                                            Box(modifier = Modifier.weight(1f)) {
                                                                                GridGameCard(
                                                                                    game = game,
                                                                                    columns = cols,
                                                                                    onLaunch = { launchGame(game) },
                                                                                    onLongClick = { longClickGame(game) }
                                                                                )
                                                                            }
                                                                        }
                                                                        repeat(cols - rowGames.size) {
                                                                            Spacer(modifier = Modifier.weight(1f))
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                            items(if (searchQuery.isEmpty()) normalGames else displayGames, key = { it.packageName }) { game ->
                                                GridGameCard(
                                                    game = game,
                                                    columns = if (isExpandedScreen) 2 else gridColumns.intValue,
                                                    onLaunch = { launchGame(game) },
                                                    onLongClick = { longClickGame(game) }
                                                )
                                            }

                                            if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                item(span = { GridItemSpan(maxLineSpan) }) {
                                                    GetMoreGamesCard(context)
                                                }
                                            }
                                        }
                                    }
                                    ViewType.List -> {
                                        if (isExpandedScreen) {
                                            LazyVerticalGrid(
                                                columns = GridCells.Adaptive(minSize = 340.dp),
                                                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                                            shape = RoundedCornerShape(28.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                            ),
                                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(vertical = 16.dp)) {
                                                                Text(
                                                                    text = stringResource(R.string.favorites),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontFamily = GoogleSansFlex,
                                                                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp)
                                                                )

                                                                val availableWidth = configuration.screenWidthDp - 168
                                                                val cols = maxOf(1, availableWidth / 340)
                                                                val chunkedFavorites = favoriteGames.chunked(cols)

                                                                Column(
                                                                    modifier = Modifier.padding(horizontal = 16.dp),
                                                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                                                ) {
                                                                    chunkedFavorites.forEach { rowGames ->
                                                                        Row(
                                                                            modifier = Modifier.fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                                        ) {
                                                                            rowGames.forEach { game ->
                                                                                Box(modifier = Modifier.weight(1f)) {
                                                                                    GameListItem(
                                                                                        game = game,
                                                                                        isSingle = true,
                                                                                        isFirst = true,
                                                                                        isLast = true,
                                                                                        onLaunch = { launchGame(game) },
                                                                                        onLongClick = { longClickGame(game) }
                                                                                    )
                                                                                }
                                                                            }
                                                                            repeat(cols - rowGames.size) {
                                                                                Spacer(modifier = Modifier.weight(1f))
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                items(items = if (searchQuery.isEmpty()) normalGames else displayGames, key = { it.packageName }) { game ->
                                                    GameListItem(
                                                        game = game,
                                                        isSingle = true,
                                                        isFirst = true,
                                                        isLast = true,
                                                        onLaunch = { launchGame(game) },
                                                        onLongClick = { longClickGame(game) }
                                                    )
                                                }

                                                if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                    item(span = { GridItemSpan(maxLineSpan) }) {
                                                        Spacer(modifier = Modifier.height(12.dp))
                                                        GetMoreGamesCard(context)
                                                    }
                                                }
                                            }
                                        } else {
                                            LazyColumn(
                                                state = listState,
                                                contentPadding = PaddingValues(start = 20.dp, top = 0.dp, end = 20.dp, bottom = 100.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.fillMaxSize()
                                            ) {
                                                if (favoriteGames.isNotEmpty() && searchQuery.isEmpty()) {
                                                    item {
                                                        Card(
                                                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                                            shape = RoundedCornerShape(28.dp),
                                                            colors = CardDefaults.cardColors(
                                                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                                            ),
                                                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                                        ) {
                                                            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                                                                Text(
                                                                    text = stringResource(R.string.favorites),
                                                                    style = MaterialTheme.typography.titleMedium,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    fontFamily = GoogleSansFlex,
                                                                    modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
                                                                )
                                                                favoriteGames.forEachIndexed { index, game ->
                                                                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
                                                                        GameListItem(
                                                                            game = game,
                                                                            isSingle = favoriteGames.size == 1,
                                                                            isFirst = index == 0,
                                                                            isLast = index == favoriteGames.size - 1,
                                                                            onLaunch = { launchGame(game) },
                                                                            onLongClick = { longClickGame(game) }
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }

                                                val currentList = if (searchQuery.isEmpty()) normalGames else displayGames
                                                itemsIndexed(items = currentList, key = { _, item -> item.packageName }) { index, game ->
                                                    GameListItem(
                                                        game = game,
                                                        isSingle = currentList.size == 1,
                                                        isFirst = index == 0,
                                                        isLast = index == currentList.size - 1,
                                                        onLaunch = { launchGame(game) },
                                                        onLongClick = { longClickGame(game) }
                                                    )
                                                }

                                                if (searchQuery.isEmpty() && showGetMoreGames.value) {
                                                    item { Spacer(modifier = Modifier.height(12.dp)); GetMoreGamesCard(context) }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (moreBottomSheetGame != null) {
        val game = moreBottomSheetGame!!
        val isFavList = game.isFavorite && searchQuery.isEmpty()
        val gCount = if (isFavList) favoriteGames.size else normalGames.size

        MoreBottomSheet(
            game = game,
            gamesCount = gCount,
            onDismiss = { moreBottomSheetGame = null },
            onEditClick = {
                moreBottomSheetGame = null
                gameToEdit = game
            },
            onStoreClick = {
                moreBottomSheetGame = null
                openPlayStore(game.packageName)
            },
            onDeleteClick = {
                moreBottomSheetGame = null
                gameToRemove = game
            },
            onMoveUp = { moveGameUp(game) },
            onMoveDown = { moveGameDown(game) }
        )
    }

    if (gameToRemove != null) {
        DeletePopup(
            game = gameToRemove!!,
            onConfirm = {
                gameToRemove?.let { viewModel.hideGame(context, it.packageName) }
                gameToRemove = null
            },
            onUninstall = {
                gameToRemove?.let {
                    try {
                        val intent = Intent(Intent.ACTION_DELETE, Uri.parse("package:${it.packageName}"))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                gameToRemove = null
            },
            onDismiss = { gameToRemove = null }
        )
    }

    if (gameToEdit != null) {
        EditAppBottomSheet(
            game = gameToEdit!!,
            onDismiss = { gameToEdit = null }
        )
    }

    if (showAddSheet) {
        AddAppsBottomSheet(
            allApps = viewModel.allApps.collectAsState().value,
            onDismiss = { showAddSheet = false },
            onAdd = { pkgs ->
                pkgs.forEach { pkg ->
                    viewModel.addManualGame(context, pkg)
                }
                showAddSheet = false
            }
        )
    }

    if (showCommunitySheet) {
        CommunityBottomSheet(
            onDismiss = {
                showCommunitySheet = false
                prefs.edit().putString("last_version", currentVersionName).apply()
            }
        )
    }
}