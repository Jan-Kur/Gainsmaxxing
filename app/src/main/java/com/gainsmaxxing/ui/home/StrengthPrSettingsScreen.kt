package com.gainsmaxxing.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.composables.icons.lucide.ArrowLeft
import com.composables.icons.lucide.Check
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.gainsmaxxing.data.repository.StrengthPrRepository
import com.gainsmaxxing.ui.components.clickableNoRipple
import com.gainsmaxxing.ui.theme.Amber500
import com.gainsmaxxing.ui.theme.BgBase
import com.gainsmaxxing.ui.theme.BorderSubtle
import com.gainsmaxxing.ui.theme.Green500
import com.gainsmaxxing.ui.theme.Surface1
import com.gainsmaxxing.ui.theme.Surface2
import com.gainsmaxxing.ui.theme.TextPrimary
import com.gainsmaxxing.ui.theme.TextSecondary
import com.gainsmaxxing.ui.theme.TextTertiary
import com.gainsmaxxing.ui.theme.caption
import com.gainsmaxxing.ui.theme.screenTitle

@Composable
fun StrengthPrSettingsScreen(
    onClose: () -> Unit,
    viewModel: StrengthPrSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val addExercise = {
        if (viewModel.addExercise()) {
            dismissKeyboard()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.load()
    }

    BackHandler(onBack = onClose)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { dismissKeyboard() })
            }
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .clickableNoRipple(onClose),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Lucide.ArrowLeft, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Strength Records",
                style = MaterialTheme.typography.screenTitle,
                color = TextPrimary,
            )
        }

        Text(
            text = "Add exercises to your catalog, drag to reorder, choose up to ${StrengthPrRepository.MAX_SELECTION} to show on Home, and delete to remove all history.",
            style = MaterialTheme.typography.caption,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        ReorderableExerciseCatalog(
            catalog = uiState.catalog,
            selected = uiState.selected,
            newExerciseName = uiState.newExerciseName,
            errorMessage = uiState.errorMessage,
            onNewExerciseNameChange = viewModel::setNewExerciseName,
            onAddExercise = addExercise,
            onToggleDisplay = viewModel::toggleDisplay,
            onDelete = viewModel::deleteExercise,
            onMoveExercise = viewModel::moveExercise,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            com.gainsmaxxing.ui.workout.WorkoutCtaButton(
                label = if (uiState.isSaving) "Saving…" else "Save",
            ) {
                if (!uiState.isSaving) {
                    viewModel.save(onComplete = onClose)
                }
            }
        }
    }
}

@Composable
private fun ReorderableExerciseCatalog(
    catalog: List<String>,
    selected: Set<String>,
    newExerciseName: String,
    errorMessage: String?,
    onNewExerciseNameChange: (String) -> Unit,
    onAddExercise: () -> Unit,
    onToggleDisplay: (String) -> Unit,
    onDelete: (String) -> Unit,
    onMoveExercise: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingKey by remember { mutableStateOf<String?>(null) }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx = remember { mutableStateMapOf<String, Float>() }
    val density = LocalDensity.current
    val itemSpacingPx = with(density) { 10.dp.toPx() }
    val catalogState = rememberUpdatedState(catalog)
    val onMoveState = rememberUpdatedState(onMoveExercise)

    fun endDrag() {
        draggingKey = null
        dragIndex = -1
        dragOffset = 0f
    }

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp),
        userScrollEnabled = draggingKey == null,
    ) {
        if (catalog.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "No exercises yet. Add one below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        itemsIndexed(catalog, key = { _, name -> name }) { _, name ->
            val isSelected = name in selected
            val canSelect = isSelected || selected.size < StrengthPrRepository.MAX_SELECTION
            val isDragging = draggingKey == name

            ExerciseCatalogRow(
                name = name,
                isSelected = isSelected,
                canSelect = canSelect,
                onToggleDisplay = { onToggleDisplay(name) },
                onDelete = { onDelete(name) },
                dragHandleModifier = Modifier.pointerInput(name) {
                    detectDragGestures(
                        onDragStart = {
                            draggingKey = name
                            dragIndex = catalogState.value.indexOf(name)
                            dragOffset = 0f
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            if (draggingKey != name) return@detectDragGestures

                            dragOffset += dragAmount.y

                            val currentCatalog = catalogState.value
                            var currentIndex = dragIndex
                            if (currentIndex !in currentCatalog.indices) return@detectDragGestures

                            val itemHeight = itemHeightsPx[name] ?: return@detectDragGestures
                            val step = itemHeight + itemSpacingPx

                            while (dragOffset > step / 2f && currentIndex < currentCatalog.lastIndex) {
                                onMoveState.value(currentIndex, currentIndex + 1)
                                currentIndex++
                                dragIndex = currentIndex
                                dragOffset -= step
                            }
                            while (dragOffset < -step / 2f && currentIndex > 0) {
                                onMoveState.value(currentIndex, currentIndex - 1)
                                currentIndex--
                                dragIndex = currentIndex
                                dragOffset += step
                            }
                        },
                        onDragEnd = {
                            if (draggingKey == name) endDrag()
                        },
                        onDragCancel = {
                            if (draggingKey == name) endDrag()
                        },
                    )
                },
                modifier = Modifier
                    .onSizeChanged { size ->
                        itemHeightsPx[name] = size.height.toFloat()
                    }
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        if (isDragging) {
                            shadowElevation = with(density) { 6.dp.toPx() }
                            alpha = 0.96f
                        }
                    },
            )
        }

        item(key = "add") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Surface1)
                    .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicTextField(
                    value = newExerciseName,
                    onValueChange = onNewExerciseNameChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    cursorBrush = SolidColor(Green500),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddExercise() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newExerciseName.isEmpty()) {
                            Text("New exercise name", color = TextTertiary, style = MaterialTheme.typography.bodyMedium)
                        }
                        inner()
                    },
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Surface2)
                        .clickableNoRipple(onAddExercise),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Plus, contentDescription = "Add exercise", tint = Green500, modifier = Modifier.size(16.dp))
                }
            }
        }

        if (errorMessage != null) {
            item(key = "error") {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.caption,
                    color = Amber500,
                )
            }
        }
    }
}

@Composable
private fun ExerciseCatalogRow(
    name: String,
    isSelected: Boolean,
    canSelect: Boolean,
    onToggleDisplay: () -> Unit,
    onDelete: () -> Unit,
    dragHandleModifier: Modifier,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            Lucide.GripVertical,
            contentDescription = "Drag to reorder",
            tint = TextTertiary,
            modifier = dragHandleModifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        DisplayToggle(
            isSelected = isSelected,
            enabled = canSelect,
            onClick = onToggleDisplay,
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isSelected) TextPrimary else TextSecondary,
            )
            if (isSelected) {
                Text(
                    text = "Shown on Home",
                    style = MaterialTheme.typography.caption,
                    color = Green500,
                )
            }
        }
        Icon(
            Lucide.Trash2,
            contentDescription = "Delete exercise and history",
            tint = TextTertiary,
            modifier = Modifier
                .size(18.dp)
                .clickableNoRipple(onDelete),
        )
    }
}

@Composable
private fun DisplayToggle(
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = when {
        isSelected -> Green500
        enabled -> BorderSubtle
        else -> BorderSubtle.copy(alpha = 0.5f)
    }
    val backgroundColor = if (isSelected) Green500.copy(alpha = 0.15f) else Color.Transparent
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.5.dp, borderColor, CircleShape)
            .then(
                if (enabled || isSelected) {
                    Modifier.clickableNoRipple(onClick)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(Lucide.Check, contentDescription = null, tint = Green500, modifier = Modifier.size(14.dp))
        }
    }
}
