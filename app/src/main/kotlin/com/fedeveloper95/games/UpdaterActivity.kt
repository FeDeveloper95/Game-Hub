@file:OptIn(
    ExperimentalTextApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.fedeveloper95.games

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.elements.UI.ExpressiveIconButton
import com.fedeveloper95.games.elements.ui.GameHubTheme
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.SettingsActivity.UpdateStatus
import com.fedeveloper95.games.services.SettingsActivity.Updater
import dev.jeziellago.compose.markdowntext.MarkdownText
import kotlinx.coroutines.launch

class UpdaterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameHubTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    UpdaterScreen(onBack = { finish() })
                }
            }
        }
    }
}

@Composable
fun AnimatedActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    leadingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    buttonHeight: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerPercent by animateIntAsState(
        targetValue = if (isPressed) 20 else 50,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "btnMorph"
    )

    Button(
        onClick = onClick,
        modifier = modifier.height(buttonHeight),
        shape = RoundedCornerShape(cornerPercent),
        enabled = enabled,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontFamily = GoogleSansFlex,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@SuppressLint("UnspecifiedRegisterReceiverFlag")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UpdaterScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var status by remember { mutableStateOf<UpdateStatus>(UpdateStatus.Idle) }
    var isDownloading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val errorMessage = stringResource(R.string.update_error_msg)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val currentVersionName = remember {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context, intent: Intent) {
                if (intent.action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
                    if (id != -1L) {
                        val downloadManager =
                            ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val query = DownloadManager.Query().setFilterById(id)
                        val cursor = downloadManager.query(query)
                        if (cursor != null && cursor.moveToFirst()) {
                            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                            if (statusIndex != -1) {
                                val downloadStatus = cursor.getInt(statusIndex)
                                if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL) {
                                    val uri = downloadManager.getUriForDownloadedFile(id)
                                    if (uri != null) {
                                        val installIntent = Intent(Intent.ACTION_VIEW).apply {
                                            setDataAndType(
                                                uri,
                                                "application/vnd.android.package-archive"
                                            )
                                            flags =
                                                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                        }
                                        try {
                                            ctx.startActivity(installIntent)
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                        cursor?.close()
                        isDownloading = false
                    }
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                receiver,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
            )
        }

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }

    fun checkUpdates() {
        status = UpdateStatus.Checking
        scope.launch {
            var isOnline = true

            try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val network = connectivityManager.activeNetwork
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                isOnline =
                    capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            if (!isOnline) {
                status = UpdateStatus.Error
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    withDismissAction = true
                )
                return@launch
            }

            try {
                val update = Updater.checkForUpdates(currentVersionName)
                status =
                    if (update != null) UpdateStatus.Available(update) else UpdateStatus.NoUpdate
            } catch (e: Exception) {
                status = UpdateStatus.Error
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    withDismissAction = true
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        checkUpdates()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Scaffold(
            modifier = Modifier
                .widthIn(max = 700.dp)
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val topBarColor = if (status is UpdateStatus.Available) {
                    MaterialTheme.colorScheme.surfaceContainer
                } else {
                    MaterialTheme.colorScheme.background
                }

                LargeTopAppBar(
                    title = {
                        if (status is UpdateStatus.Available) {
                            val currentStatus = status as UpdateStatus.Available
                            val titleText = stringResource(R.string.update_available, currentStatus.info.version)
                            val versionText = "v${currentStatus.info.version.replace("v", "", ignoreCase = true).trim()}"

                            val fraction = scrollBehavior.state.collapsedFraction
                            val alphaValue = (1f - (fraction / 0.6f)).coerceIn(0f, 1f)

                            if (alphaValue > 0f) {
                                Row(
                                    modifier = Modifier
                                        .padding(end = 24.dp)
                                        .alpha(alphaValue),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_nine_sided_cookie),
                                            contentDescription = null,
                                            modifier = Modifier.size(60.dp),
                                            tint = MaterialTheme.colorScheme.primaryContainer
                                        )
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_update_avaiable),
                                            contentDescription = null,
                                            modifier = Modifier.size(30.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = titleText,
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = versionText,
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        Box(modifier = Modifier.padding(start = 16.dp, end = 16.dp)) {
                            ExpressiveIconButton(
                                onClick = onBack,
                                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.cancel_action),
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = topBarColor,
                        scrolledContainerColor = topBarColor
                    ),
                    scrollBehavior = scrollBehavior,
                    modifier = Modifier.clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                )
            },
            bottomBar = {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(WindowInsets.navigationBars.asPaddingValues())
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedActionButton(
                            text = stringResource(R.string.see_source_code),
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://github.com/FeDeveloper95/Game-Hub")
                                )
                                context.startActivity(intent)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            leadingIcon = {
                                Icon(
                                    imageVector = ImageVector.vectorResource(R.drawable.ic_github),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            buttonHeight = 48.dp,
                            enabled = !isDownloading
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        when (val currentStatus = status) {
                            is UpdateStatus.Idle, is UpdateStatus.Checking, is UpdateStatus.NoUpdate, is UpdateStatus.Error -> {
                                AnimatedActionButton(
                                    text = stringResource(R.string.check_updates_action),
                                    onClick = { checkUpdates() },
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary,
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Rounded.Sync,
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    enabled = currentStatus !is UpdateStatus.Checking && !isDownloading,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            is UpdateStatus.Available -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AnimatedActionButton(
                                        text = stringResource(R.string.later),
                                        onClick = onBack,
                                        modifier = Modifier.weight(1f),
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        enabled = !isDownloading
                                    )
                                    AnimatedActionButton(
                                        text = if (isDownloading) "Downloading..." else stringResource(
                                            R.string.update_action
                                        ),
                                        onClick = {
                                            isDownloading = true
                                            Updater.startDownload(
                                                context,
                                                currentStatus.info.downloadUrl,
                                                currentStatus.info.version
                                            )
                                        },
                                        modifier = Modifier.weight(1f),
                                        enabled = !isDownloading
                                    )
                                }
                            }
                        }
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        val boxModifier = if (status is UpdateStatus.Available) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillParentMaxSize()
                        }

                        Box(
                            modifier = boxModifier,
                            contentAlignment = Alignment.Center
                        ) {
                            when (val currentStatus = status) {
                                is UpdateStatus.Checking -> {
                                    LoadingIndicator(
                                        modifier = Modifier.size(180.dp)
                                    )
                                }

                                is UpdateStatus.NoUpdate -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_nine_sided_cookie),
                                                contentDescription = null,
                                                modifier = Modifier.size(180.dp),
                                                tint = MaterialTheme.colorScheme.primaryContainer
                                            )
                                            Icon(
                                                imageVector = ImageVector.vectorResource(R.drawable.ic_no_updates),
                                                contentDescription = null,
                                                modifier = Modifier.size(90.dp),
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = stringResource(R.string.update_latest_version_msg),
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.primary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                is UpdateStatus.Error -> {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = ImageVector.vectorResource(R.drawable.ic_no_updates),
                                            contentDescription = null,
                                            modifier = Modifier.size(120.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Text(
                                            text = stringResource(R.string.update_error_msg),
                                            fontFamily = GoogleSansFlex,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = MaterialTheme.colorScheme.error,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                is UpdateStatus.Available -> {
                                    MarkdownText(
                                        markdown = currentStatus.info.changelog,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontResource = R.font.sans_flex,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                else -> {}
                            }
                        }
                    }
                }

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 12.dp)
                )
            }
        }
    }
}