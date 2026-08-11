package com.fedeveloper95.games.elements.MainActivity

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fedeveloper95.games.R
import com.fedeveloper95.games.elements.ui.AppIcon
import com.fedeveloper95.games.elements.ui.GoogleSansFlex
import com.fedeveloper95.games.services.mainactivity.GameApp
import com.fedeveloper95.games.services.mainactivity.AllAppsCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddAppsBottomSheet(
    allApps: List<GameApp>,
    onDismiss: () -> Unit,
    onAdd: (List<String>) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedApps by remember { mutableStateOf(setOf<String>()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var cachedAllApps by remember { mutableStateOf<List<GameApp>>(emptyList()) }

    LaunchedEffect(Unit) {
        launch(Dispatchers.IO) {
            val loaded = AllAppsCacheManager.loadApps(context)
            if (loaded.isNotEmpty()) {
                cachedAllApps = loaded
            }
        }
    }

    val displayApps = if (allApps.isEmpty() && cachedAllApps.isNotEmpty()) cachedAllApps else allApps

    LaunchedEffect(allApps) {
        if (allApps.isNotEmpty()) {
            launch(Dispatchers.IO) {
                AllAppsCacheManager.saveApps(context, allApps)
            }
        }
    }

    val filteredApps = remember(searchQuery, displayApps) {
        displayApps
            .distinctBy { it.packageName }
            .filter { app ->
                app.packageName != context.packageName &&
                        (searchQuery.isEmpty() || app.name.contains(searchQuery, ignoreCase = true))
            }
    }

    BackHandler(enabled = selectedApps.isNotEmpty()) {
        selectedApps = emptySet()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.statusBarsPadding(),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.ime)
        ) {
            Text(
                text = stringResource(R.string.add_to_library),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = GoogleSansFlex,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
            )

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = {
                    Text(
                        stringResource(R.string.search_apps),
                        fontFamily = GoogleSansFlex
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, null) },
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

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxWidth().weight(1f, fill = false)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)
                ) {
                    itemsIndexed(
                        items = filteredApps,
                        key = { _, app -> app.packageName },
                        contentType = { _, _ -> "appItem" }
                    ) { index, app ->
                        val isSelected = selectedApps.contains(app.packageName)
                        val inSelectionMode = selectedApps.isNotEmpty()

                        AddAppSegmentedItem(
                            app = app,
                            isSelected = isSelected,
                            isSingle = filteredApps.size == 1,
                            index = index,
                            count = filteredApps.size,
                            onClick = {
                                if (inSelectionMode) {
                                    selectedApps = if (isSelected) selectedApps - app.packageName else selectedApps + app.packageName
                                } else {
                                    scope.launch {
                                        sheetState.hide()
                                        onAdd(listOf(app.packageName))
                                    }
                                }
                            },
                            onLongClick = {
                                selectedApps = if (isSelected) selectedApps - app.packageName else selectedApps + app.packageName
                            }
                        )
                    }
                }

                this@Column.AnimatedVisibility(
                    visible = selectedApps.isNotEmpty(),
                    enter = fadeIn(animationSpec = tween(150)),
                    exit = fadeOut(animationSpec = tween(150)),
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 16.dp, bottom = 16.dp)
                        .navigationBarsPadding()
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            scope.launch {
                                sheetState.hide()
                                onAdd(selectedApps.toList())
                            }
                        },
                        icon = { Icon(Icons.Default.Add, null) },
                        text = { Text(stringResource(R.string.add_to_library), fontFamily = GoogleSansFlex) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AddAppSegmentedItem(
    app: GameApp,
    isSelected: Boolean,
    isSingle: Boolean,
    index: Int,
    count: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val baseModifier = if (isSingle) Modifier.clip(RoundedCornerShape(20.dp)) else Modifier

    SegmentedListItem(
        selected = false,
        onClick = onClick,
        modifier = baseModifier.pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { onLongClick() },
                onTap = { onClick() }
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AppIcon(
                    packageName = app.packageName,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = app.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = GoogleSansFlex,
                        fontWeight = FontWeight.Medium
                    )
                }
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Icon(
                        Icons.Default.Add,
                        null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    )
}