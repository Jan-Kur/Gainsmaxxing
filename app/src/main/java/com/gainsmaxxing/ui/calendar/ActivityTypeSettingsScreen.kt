package com.gainsmaxxing.ui.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.GripVertical
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Plus
import com.composables.icons.lucide.Trash2
import com.gainsmaxxing.data.repository.CalendarRepository
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
import com.gainsmaxxing.ui.workout.WorkoutCtaButton

@Composable
fun ActivityTypeSettingsScreen(
    onClose: () -> Unit,
    viewModel: ActivityTypeSettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val dismissKeyboard = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val addType = {
        if (viewModel.addType()) dismissKeyboard()
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
        CalendarSettingsHeader(
            title = "Activity Types",
            onClose = onClose,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Text(
            text = "Add activity types, drag to set cycle order in Edit Calendar, and pick a color and icon for each.",
            style = MaterialTheme.typography.caption,
            color = TextTertiary,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        Spacer(Modifier.height(16.dp))

        ReorderableActivityTypeList(
            types = uiState.types,
            expandedTypeId = uiState.expandedTypeId,
            newTypeName = uiState.newTypeName,
            errorMessage = uiState.errorMessage,
            onNewTypeNameChange = viewModel::setNewTypeName,
            onAddType = addType,
            onDelete = viewModel::deleteType,
            onMove = viewModel::moveType,
            onToggleExpanded = { id ->
                viewModel.setExpandedType(if (uiState.expandedTypeId == id) null else id)
            },
            onNameChange = viewModel::updateTypeName,
            onColorChange = viewModel::updateTypeColor,
            onIconChange = viewModel::updateTypeIcon,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
        ) {
            WorkoutCtaButton(
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
private fun ReorderableActivityTypeList(
    types: List<ActivityTypeDraft>,
    expandedTypeId: Long?,
    newTypeName: String,
    errorMessage: String?,
    onNewTypeNameChange: (String) -> Unit,
    onAddType: () -> Unit,
    onDelete: (Long) -> Unit,
    onMove: (Int, Int) -> Unit,
    onToggleExpanded: (Long) -> Unit,
    onNameChange: (Long, String) -> Unit,
    onColorChange: (Long, Int) -> Unit,
    onIconChange: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draggingKey by remember { mutableStateOf<Long?>(null) }
    var dragIndex by remember { mutableIntStateOf(-1) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val itemHeightsPx = remember { mutableStateMapOf<Long, Float>() }
    val density = LocalDensity.current
    val itemSpacingPx = with(density) { 10.dp.toPx() }
    val typesState = rememberUpdatedState(types)
    val onMoveState = rememberUpdatedState(onMove)

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
        if (types.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "No activity types yet. Add one below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }

        itemsIndexed(types, key = { _, type -> type.id }) { _, type ->
            val isDragging = draggingKey == type.id
            val isExpanded = expandedTypeId == type.id

            Column(
                modifier = Modifier
                    .onSizeChanged { size ->
                        itemHeightsPx[type.id] = size.height.toFloat()
                    }
                    .zIndex(if (isDragging) 1f else 0f)
                    .graphicsLayer {
                        translationY = if (isDragging) dragOffset else 0f
                        if (isDragging) {
                            shadowElevation = with(density) { 6.dp.toPx() }
                            alpha = 0.96f
                        }
                    },
            ) {
                ActivityTypeRow(
                    type = type,
                    isExpanded = isExpanded,
                    onToggleExpanded = { onToggleExpanded(type.id) },
                    onDelete = { onDelete(type.id) },
                    onNameChange = { onNameChange(type.id, it) },
                    dragHandleModifier = Modifier.pointerInput(type.id) {
                        detectDragGestures(
                            onDragStart = {
                                draggingKey = type.id
                                dragIndex = typesState.value.indexOfFirst { it.id == type.id }
                                dragOffset = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                if (draggingKey != type.id) return@detectDragGestures

                                dragOffset += dragAmount.y
                                val currentTypes = typesState.value
                                var currentIndex = dragIndex
                                if (currentIndex !in currentTypes.indices) return@detectDragGestures

                                val itemHeight = itemHeightsPx[type.id] ?: return@detectDragGestures
                                val step = itemHeight + itemSpacingPx

                                while (dragOffset > step / 2f && currentIndex < currentTypes.lastIndex) {
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
                                if (draggingKey == type.id) endDrag()
                            },
                            onDragCancel = {
                                if (draggingKey == type.id) endDrag()
                            },
                        )
                    },
                )

                if (isExpanded) {
                    Spacer(Modifier.height(8.dp))
                    ActivityTypeAppearancePickers(
                        selectedColorIndex = type.colorPaletteIndex,
                        selectedIconKey = type.iconKey,
                        onColorChange = { onColorChange(type.id, it) },
                        onIconChange = { onIconChange(type.id, it) },
                    )
                }
            }
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
                    value = newTypeName,
                    onValueChange = onNewTypeNameChange,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
                    cursorBrush = SolidColor(Green500),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onAddType() }),
                    modifier = Modifier.weight(1f),
                    decorationBox = { inner ->
                        if (newTypeName.isEmpty()) {
                            Text(
                                "New activity name",
                                color = TextTertiary,
                                style = MaterialTheme.typography.bodyMedium,
                            )
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
                        .clickableNoRipple(onAddType),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Lucide.Plus, contentDescription = "Add activity type", tint = Green500, modifier = Modifier.size(16.dp))
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
private fun ActivityTypeRow(
    type: ActivityTypeDraft,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit,
    onDelete: () -> Unit,
    onNameChange: (String) -> Unit,
    dragHandleModifier: Modifier,
) {
    val color = activityColor(type.colorPaletteIndex)
    val icon = CalendarIcons.resolve(type.iconKey)

    Row(
        modifier = Modifier
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
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(1.5.dp, color.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
        }
        Spacer(Modifier.width(10.dp))
        BasicTextField(
            value = type.name,
            onValueChange = onNameChange,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = TextPrimary),
            cursorBrush = SolidColor(Green500),
            singleLine = true,
            modifier = Modifier.weight(1f),
        )
        Icon(
            if (isExpanded) Lucide.ChevronUp else Lucide.ChevronDown,
            contentDescription = "Edit appearance",
            tint = TextTertiary,
            modifier = Modifier
                .size(18.dp)
                .clickableNoRipple(onToggleExpanded),
        )
        Spacer(Modifier.width(4.dp))
        Icon(
            Lucide.Trash2,
            contentDescription = "Delete activity type",
            tint = TextTertiary,
            modifier = Modifier
                .size(18.dp)
                .clickableNoRipple(onDelete),
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTypeAppearancePickers(
    selectedColorIndex: Int,
    selectedIconKey: String,
    onColorChange: (Int) -> Unit,
    onIconChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Surface1)
            .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Color", style = MaterialTheme.typography.caption, color = TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ActivityColorPalette.take(CalendarRepository.ACTIVITY_COLOR_COUNT).forEachIndexed { index, color ->
                val selected = index == selectedColorIndex
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) TextPrimary else Color.Transparent,
                            shape = CircleShape,
                        )
                        .clickableNoRipple { onColorChange(index) },
                )
            }
        }

        Text("Icon", style = MaterialTheme.typography.caption, color = TextSecondary)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CalendarIcons.curated.forEach { entry ->
                val selected = entry.key == selectedIconKey
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Green500.copy(alpha = 0.15f) else Surface2)
                        .border(
                            width = 1.dp,
                            color = if (selected) Green500 else BorderSubtle,
                            shape = RoundedCornerShape(10.dp),
                        )
                        .clickableNoRipple { onIconChange(entry.key) },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(entry.icon, entry.label, tint = if (selected) Green500 else TextSecondary, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}
