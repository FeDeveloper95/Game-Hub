package com.fedeveloper95.games

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// --- GOOGLE SANS FLEX FONT (Variabile & Rounded) ---
@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.width(100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

// --- TIPOGRAFIA EXPRESSIVE ---
val ExpressiveTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 48.sp,
        lineHeight = 56.sp,
        letterSpacing = (-1).sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

// --- TEMA AUTOMATICO ---
@Composable
fun GameHubTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current

    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (darkTheme) {
            darkColorScheme(
                primary = Color(0xFFD0BCFF),
                onPrimary = Color(0xFF381E72),
                primaryContainer = Color(0xFF4F378B),
                onPrimaryContainer = Color(0xFFEADDFF),
                secondary = Color(0xFFCCC2DC),
                background = Color(0xFF141218),
                surface = Color(0xFF141218),
                surfaceContainer = Color(0xFF211F26)
            )
        } else {
            lightColorScheme(
                primary = Color(0xFF6750A4),
                onPrimary = Color.White,
                primaryContainer = Color(0xFFEADDFF),
                onPrimaryContainer = Color(0xFF21005D),
                secondary = Color(0xFF625B71),
                background = Color(0xFFF9F9FF),
                surface = Color(0xFFF9F9FF),
                surfaceContainer = Color(0xFFE7E0EC)
            )
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()

            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        content = content
    )
}

// --- ANIMAZIONI ---
@Composable
fun animateCornerPercentAsState(
    isPressed: Boolean,
    defaultPercent: Int = 50,
    pressedPercent: Int = 20
): State<Int> {
    return animateIntAsState(
        targetValue = if (isPressed) pressedPercent else defaultPercent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cornerMorph"
    )
}

// --- DATI ---
data class GameApp(
    val name: String,
    val packageName: String,
    val icon: Drawable?,
    val launchIntent: Intent?
)

// --- VIEWMODEL ---
class GameViewModel : ViewModel() {
    private val _games = MutableStateFlow<List<GameApp>>(emptyList())
    val games: StateFlow<List<GameApp>> = _games

    private val _allApps = MutableStateFlow<List<GameApp>>(emptyList())
    val allApps: StateFlow<List<GameApp>> = _allApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val HIDDEN_GAMES_PREF = "hidden_games"
    private val MANUAL_GAMES_PREF = "manual_games"

    private var loadJob: Job? = null

    fun loadGames(context: Context) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true

            val packageManager = context.packageManager
            val loadedData = withContext(Dispatchers.IO) {
                try {
                    val hiddenGames = getPrefsSet(context, HIDDEN_GAMES_PREF)
                    val manualGames = getPrefsSet(context, MANUAL_GAMES_PREF)

                    val intent = Intent(Intent.ACTION_MAIN, null)
                    intent.addCategory(Intent.CATEGORY_LAUNCHER)
                    val resolveInfos = packageManager.queryIntentActivities(intent, 0)

                    val gamesList = mutableListOf<GameApp>()
                    val allAppsList = mutableListOf<GameApp>()

                    for (resolveInfo in resolveInfos) {
                        val packageName = resolveInfo.activityInfo.packageName ?: continue
                        val name = resolveInfo.loadLabel(packageManager).toString()
                        val icon = resolveInfo.loadIcon(packageManager)
                        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: continue

                        val appInfo = try {
                            packageManager.getApplicationInfo(packageName, 0)
                        } catch (e: PackageManager.NameNotFoundException) { continue }

                        val isDeclaredGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            appInfo.category == ApplicationInfo.CATEGORY_GAME
                        } else {
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                        }

                        val isManualGame = manualGames.contains(packageName)
                        val isHidden = hiddenGames.contains(packageName)

                        val app = GameApp(name, packageName, icon, launchIntent)

                        if (!isDeclaredGame || isHidden) {
                            if (!isManualGame) allAppsList.add(app)
                        }

                        if ((isDeclaredGame || isManualGame) && !isHidden) {
                            gamesList.add(app)
                        }
                    }
                    Pair(gamesList.sortedBy { it.name }, allAppsList.sortedBy { it.name })
                } catch (e: Exception) {
                    Pair(emptyList(), emptyList())
                }
            }

            _games.value = loadedData.first
            _allApps.value = loadedData.second
            _isLoading.value = false
        }
    }

    fun hideGame(context: Context, packageName: String) {
        removeFromPrefs(context, MANUAL_GAMES_PREF, packageName)
        addToPrefs(context, HIDDEN_GAMES_PREF, packageName)
        loadGames(context)
    }

    fun addManualGame(context: Context, packageName: String) {
        removeFromPrefs(context, HIDDEN_GAMES_PREF, packageName)
        addToPrefs(context, MANUAL_GAMES_PREF, packageName)
        loadGames(context)
    }

    private fun getPrefsSet(context: Context, key: String): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet(key, emptySet()) ?: emptySet()
    }

    private fun addToPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        set.add(value)
        prefs.edit().putStringSet(key, set).apply()
    }

    private fun removeFromPrefs(context: Context, key: String, value: String) {
        val prefs = context.getSharedPreferences("game_hub_prefs", Context.MODE_PRIVATE)
        val set = prefs.getStringSet(key, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        if (set.contains(value)) {
            set.remove(value)
            prefs.edit().putStringSet(key, set).apply()
        }
    }
}

// --- ACTIVITY ---
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    GameHubScreen()
                }
            }
        }
    }
}

// --- UI ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameHubScreen(viewModel: GameViewModel = viewModel()) {
    val context = LocalContext.current
    val games by viewModel.games.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showAddSheet by remember { mutableStateOf(false) }
    var gameToRemove by remember { mutableStateOf<GameApp?>(null) }

    // Manteniamo lo scroll behavior SOLO per il collasso del titolo
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.loadGames(context)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (scrollBehavior.state.collapsedFraction < 0.5f) {
                            Text(
                                text = "${games.size} ${stringResource(R.string.games_count_suffix)}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddSheet = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 88.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_game))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            if (games.isEmpty() && !isLoading) {
                EmptyState()
            } else if (isLoading && games.isEmpty()) {
                // Indicatore di caricamento solo iniziale se la lista è vuota
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(items = games, key = { it.packageName }) { game ->
                        SwipeToDeleteContainer(item = game, onDelete = { gameToRemove = game }) {
                            GameListItem(game = game, onLaunch = {
                                game.launchIntent?.let { context.startActivity(it) }
                            })
                        }
                    }
                }
            }

            Surface(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://search?q=games&c=apps") }
                    try { context.startActivity(intent) } catch (e: Exception) {}
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .height(64.dp)
                    .widthIn(min = 220.dp, max = 320.dp)
                    .zIndex(1f),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp,
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        stringResource(R.string.download_games),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (gameToRemove != null) {
        AlertDialog(
            onDismissRequest = { gameToRemove = null },
            icon = { Icon(Icons.Default.Delete, null) },
            title = { Text(stringResource(R.string.remove_game_title)) },
            text = { Text(stringResource(R.string.remove_game_desc, gameToRemove!!.name)) },
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            confirmButton = {
                Button(
                    onClick = {
                        gameToRemove?.let { viewModel.hideGame(context, it.packageName) }
                        gameToRemove = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text(stringResource(R.string.remove)) }
            },
            dismissButton = {
                TextButton(onClick = { gameToRemove = null }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showAddSheet) {
        AddGameBottomSheet(
            allApps = viewModel.allApps.collectAsState().value,
            onDismiss = { showAddSheet = false },
            onAdd = { pkg ->
                viewModel.addManualGame(context, pkg)
                showAddSheet = false
            }
        )
    }
}

// --- BOTTOM SHEET ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGameBottomSheet(
    allApps: List<GameApp>,
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)

    val filteredApps = remember(searchQuery, allApps) {
        if (searchQuery.isEmpty()) allApps
        else allApps.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
        scrimColor = Color.Black.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp)
        ) {
            Text(
                stringResource(R.string.add_to_library),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                placeholder = { Text(stringResource(R.string.search_apps)) },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(filteredApps) { index, app ->
                    val shape = when {
                        filteredApps.size == 1 -> RoundedCornerShape(28.dp)
                        index == 0 -> RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp, bottomStart = 2.dp, bottomEnd = 2.dp)
                        index == filteredApps.size - 1 -> RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp, bottomStart = 28.dp, bottomEnd = 28.dp)
                        else -> RoundedCornerShape(2.dp)
                    }

                    GroupedAppItem(app = app, shape = shape, onAdd = { onAdd(app.packageName) })
                }
            }
        }
    }
}

@Composable
fun GroupedAppItem(
    app: GameApp,
    shape: Shape,
    onAdd: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    Surface(
        onClick = onAdd,
        shape = shape,
        color = if (isPressed) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow,
        interactionSource = interactionSource,
        modifier = Modifier.fillMaxWidth().height(72.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = app.icon,
                contentDescription = null,
                modifier = Modifier.size(42.dp).clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = app.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
        }
    }
}

@Composable
fun GameListItem(
    game: GameApp,
    onLaunch: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 10 else 28,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardMorph"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(104.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onLaunch),
        shape = RoundedCornerShape(cornerPercent.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.8f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = game.icon,
                contentDescription = null,
                modifier = Modifier.size(68.dp).clip(RoundedCornerShape(22.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = game.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            AnimatedPlayButton(onClick = onLaunch)
        }
    }
}

@Composable
fun AnimatedPlayButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp),
        shape = RoundedCornerShape(cornerPercent),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        interactionSource = interactionSource,
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Text(
            text = stringResource(R.string.play),
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    item: GameApp,
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                return@rememberSwipeToDismissBoxState false
            }
            false
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by animateColorAsState(
                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) MaterialTheme.colorScheme.errorContainer else Color.Transparent
            )
            Box(
                Modifier.fillMaxSize().clip(RoundedCornerShape(28.dp)).background(color).padding(start = 24.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (dismissState.targetValue == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(Icons.Default.Delete, stringResource(R.string.remove_action), tint = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
        },
        enableDismissFromEndToStart = false,
        content = { content() }
    )
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Rounded.SportsEsports, null, modifier = Modifier.size(120.dp), tint = MaterialTheme.colorScheme.surfaceVariant)
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.no_games_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface)
        Text(stringResource(R.string.no_games_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}