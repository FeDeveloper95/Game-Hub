package com.fedeveloper95.games.elements.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VideogameAsset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.fedeveloper95.games.R
import com.fedeveloper95.games.services.mainactivity.GameApp

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlex = FontFamily(
    Font(
        resId = R.font.sans_flex,
        weight = FontWeight.Normal,
        style = FontStyle.Normal,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(400),
            FontVariation.Setting("wdth", 100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

@OptIn(ExperimentalTextApi::class)
val GoogleSansFlexThin = FontFamily(
    Font(
        resId = R.font.sans_flex,
        variationSettings = FontVariation.Settings(
            FontVariation.Setting("GRAD", 100f),
            FontVariation.Setting("ROND", 100f)
        )
    )
)

val ExpressiveTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 42.sp,
        lineHeight = 48.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = GoogleSansFlex,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
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
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
)

@Composable
fun GameHubTheme(
    themeOverride: Int? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { context.getSharedPreferences("game_hub_settings", Context.MODE_PRIVATE) }

    val savedTheme = remember { mutableIntStateOf(prefs.getInt("pref_theme", 0)) }

    DisposableEffect(Unit) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key == "pref_theme") {
                savedTheme.intValue = sharedPreferences.getInt("pref_theme", 0)
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        onDispose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    val activeTheme = themeOverride ?: savedTheme.intValue

    val darkTheme = when (activeTheme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }

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

@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    contentColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        animationSpec = tween(durationMillis = 200),
        label = "corner"
    )

    val borderModifier = if (isFocused) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerPercent)) else Modifier

    Surface(
        onClick = onClick,
        modifier = Modifier.size(44.dp).then(borderModifier),
        shape = RoundedCornerShape(cornerPercent),
        color = containerColor,
        contentColor = contentColor,
        interactionSource = interactionSource
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = contentDescription)
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun MainHeaderTitle(showUserName: Boolean, userName: String, modifier: Modifier = Modifier) {
    val customWelcomeFontFamily = FontFamily(
        Font(
            resId = R.font.sans_flex,
            variationSettings = FontVariation.Settings(
                FontVariation.Setting("slnt", -9f),
                FontVariation.Setting("wdth", 111f),
                FontVariation.weight(333),
                FontVariation.Setting("GRAD", 100f),
                FontVariation.Setting("ROND", 100f)
            )
        )
    )

    Column(
        modifier = modifier
    ) {
        if (showUserName) {
            Text(
                text = "$userName's",
                style = TextStyle(
                    fontFamily = customWelcomeFontFamily,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Text(
            text = stringResource(R.string.app_name),
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            fontSize = 42.sp,
            color = MaterialTheme.colorScheme.primary,
            lineHeight = 48.sp
        )
    }
}

@Composable
fun SearchAndCountHeader(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    count: Int,
    showSearch: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        if (showSearch) {
            HomeSearchBar(
                query = searchQuery,
                onQueryChange = onQueryChange
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
        Text(
            text = "$count ${stringResource(R.string.games_count_suffix)}",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun HomeSearchBar(query: String, onQueryChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionDown -> {
                            focusManager.moveFocus(FocusDirection.Down)
                            true
                        }
                        Key.DirectionUp -> {
                            focusManager.moveFocus(FocusDirection.Up)
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            },
        placeholder = { Text(stringResource(R.string.search_apps), fontFamily = GoogleSansFlex) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = GoogleSansFlex)
    )
}

@Composable
fun AnimatedPlayButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 15 else 50,
        animationSpec = tween(durationMillis = 200),
        label = "btnMorph"
    )

    val borderModifier = if (isFocused) Modifier.border(3.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(cornerPercent)) else Modifier

    Button(
        onClick = onClick,
        modifier = Modifier.height(40.dp).then(borderModifier),
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

fun Color.darken(factor: Float = 0.8f): Color {
    return Color(
        red = this.red * factor,
        green = this.green * factor,
        blue = this.blue * factor,
        alpha = this.alpha
    )
}

@Composable
fun GameIconDisplay(game: GameApp, modifier: Modifier = Modifier) {
    val customIconUri = game.customIconUri
    val isBuiltIn = customIconUri?.startsWith("builtin://") == true

    if (isBuiltIn) {
        val uriStr = customIconUri!!
        val name = uriStr.substringAfter("builtin://").substringBefore("#")
        val colorHex = uriStr.substringAfter("#").toIntOrNull() ?: 0
        val color = if (colorHex != 0) Color(colorHex) else MaterialTheme.colorScheme.primary

        val iconVector = when(name) {
            "star" -> Icons.Rounded.Star
            "controller" -> Icons.Rounded.SportsEsports
            "bolt" -> Icons.Rounded.Bolt
            "heart" -> Icons.Rounded.Favorite
            "gamepad" -> Icons.Rounded.Gamepad
            "videogame" -> Icons.Rounded.VideogameAsset
            "toy" -> Icons.Rounded.SmartToy
            "puzzle" -> Icons.Rounded.Extension
            "dice" -> Icons.Rounded.Casino
            "rocket" -> Icons.Rounded.RocketLaunch
            "trophy" -> Icons.Rounded.EmojiEvents
            "explore" -> Icons.Rounded.Explore
            else -> Icons.Rounded.Gamepad
        }

        Box(
            modifier = modifier.background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = iconVector,
                contentDescription = null,
                tint = color.darken(0.5f),
                modifier = Modifier.fillMaxSize(0.56f)
            )
        }
    } else {
        AppIcon(
            packageName = game.packageName,
            customIconUri = customIconUri,
            modifier = modifier
        )
    }
}

@SuppressLint("UseKtx")
@Composable
fun GetMoreGamesCard(context: Context) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnMorph"
    )

    val borderModifier = if (isFocused) Modifier.border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(cornerPercent)) else Modifier

    Surface(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW).apply { data = Uri.parse("market://search?q=games&c=apps") }
            try { context.startActivity(intent) } catch (e: Exception) {}
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 24.dp)
            .then(borderModifier),
        shape = RoundedCornerShape(cornerPercent),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 8.dp,
        shadowElevation = 6.dp,
        interactionSource = interactionSource
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Outlined.ShoppingBag,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.download_games),
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                fontFamily = GoogleSansFlex
            )
        }
    }
}

@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = ImageVector.vectorResource(R.drawable.ic_launcher_monochrome),
            contentDescription = null,
            modifier = Modifier.size(180.dp),
            tint = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(stringResource(R.string.no_games_title), style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.onSurface, fontFamily = GoogleSansFlex)
        Text(stringResource(R.string.no_games_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = GoogleSansFlex)
    }
}