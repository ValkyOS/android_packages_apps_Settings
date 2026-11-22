/*
 * SPDX-FileCopyrightText: 2026 kenway214
 * SPDX-FileCopyrightText: crDroid Android Project
 * SPDX-License-Identifier: Apache-2.0
 */
package com.android.settings.display.clock

import android.app.WallpaperManager
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.os.UserHandle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import com.android.settings.R
import com.android.settingslib.spa.framework.theme.SettingsTheme
import java.util.WeakHashMap
import kotlin.math.roundToInt

/**
 * Lock screen clock picker, ported from crDroid. Shows every clock style over
 * the wallpaper; SystemUI restarts itself to load the chosen style.
 */
class ClockPickerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            isNestedScrollingEnabled = true
            setContent {
                SettingsTheme {
                    val initialClock = remember {
                        Settings.Secure.getIntForUser(
                            context.contentResolver,
                            ClockUtils.SETTING_CLOCK_STYLE,
                            0,
                            UserHandle.USER_CURRENT
                        )
                    }
                    val initialSize = remember {
                        Settings.Secure.getIntForUser(
                            context.contentResolver,
                            ClockUtils.SETTING_CLOCK_SIZE,
                            ClockUtils.DEFAULT_CLOCK_SIZE,
                            UserHandle.USER_CURRENT
                        ).coerceIn(ClockUtils.MIN_CLOCK_SIZE, ClockUtils.MAX_CLOCK_SIZE)
                    }
                    val initialColorMode = remember {
                        Settings.Secure.getStringForUser(
                            context.contentResolver,
                            ClockUtils.SETTING_CLOCK_COLOR_MODE,
                            UserHandle.USER_CURRENT
                        ) ?: ClockUtils.COLOR_MODE_DEFAULT
                    }
                    val initialCustomColor = remember {
                        Settings.Secure.getIntForUser(
                            context.contentResolver,
                            ClockUtils.SETTING_CLOCK_CUSTOM_COLOR,
                            AndroidColor.WHITE,
                            UserHandle.USER_CURRENT
                        )
                    }
                    ClockPickerScreen(
                        initialClock = initialClock,
                        initialSize = initialSize,
                        initialColorMode = initialColorMode,
                        initialCustomColor = initialCustomColor,
                        onApply = { clockStyle, clockSize, colorMode, customColor ->
                            applyClock(clockStyle, clockSize, colorMode, customColor)
                            requireActivity().onBackPressedDispatcher.onBackPressed()
                        }
                    )
                }
            }
        }
    }

    private fun applyClock(
        clockStyle: Int,
        clockSize: Int,
        colorMode: String,
        customColor: Int
    ) {
        val ctx = requireContext()
        val currentClock = Settings.Secure.getIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_STYLE,
            0,
            UserHandle.USER_CURRENT
        )
        val currentSize = Settings.Secure.getIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_SIZE,
            ClockUtils.DEFAULT_CLOCK_SIZE,
            UserHandle.USER_CURRENT
        )
        val currentColorMode = Settings.Secure.getStringForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_COLOR_MODE,
            UserHandle.USER_CURRENT
        ) ?: ClockUtils.COLOR_MODE_DEFAULT
        val currentCustomColor = Settings.Secure.getIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_CUSTOM_COLOR,
            AndroidColor.WHITE,
            UserHandle.USER_CURRENT
        )
        if (clockStyle == currentClock && clockSize == currentSize &&
            colorMode == currentColorMode && customColor == currentCustomColor
        ) return

        Toast.makeText(ctx, R.string.valkyos_clock_style_restart, Toast.LENGTH_SHORT).show()
        Settings.Secure.putIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_STYLE,
            clockStyle,
            UserHandle.USER_CURRENT
        )
        Settings.Secure.putIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_SIZE,
            clockSize,
            UserHandle.USER_CURRENT
        )
        Settings.Secure.putStringForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_COLOR_MODE,
            colorMode,
            UserHandle.USER_CURRENT
        )
        Settings.Secure.putIntForUser(
            ctx.contentResolver,
            ClockUtils.SETTING_CLOCK_CUSTOM_COLOR,
            customColor,
            UserHandle.USER_CURRENT
        )
    }
}

@Composable
fun ClockPickerScreen(
    initialClock: Int,
    initialSize: Int,
    initialColorMode: String,
    initialCustomColor: Int,
    onApply: (Int, Int, String, Int) -> Unit
) {
    var selectedClock by rememberSaveable { mutableStateOf(initialClock) }
    var selectedSize by rememberSaveable { mutableIntStateOf(initialSize) }
    var selectedColorMode by rememberSaveable { mutableStateOf(initialColorMode) }
    var selectedCustomColor by rememberSaveable { mutableIntStateOf(initialCustomColor) }
    val clockNames = stringArrayResource(R.array.valkyos_clock_style_entries)
    val clockLayouts = remember { ClockUtils.CLOCK_LAYOUTS }
    val context = LocalContext.current
    val hostView = LocalView.current

    val wallpaperBitmap: ImageBitmap? = remember {
        runCatching {
            val wm = WallpaperManager.getInstance(context)
            (wm.getDrawable(WallpaperManager.FLAG_LOCK)
                ?: wm.getDrawable(WallpaperManager.FLAG_SYSTEM))
                ?.toBitmap(240, 480)
                ?.asImageBitmap()
        }.getOrNull()
    }

    val nestedScrollInterop = remember(hostView) {
        ComposeToViewNestedScrollBridge(hostView)
    }
    DisposableEffect(nestedScrollInterop) {
        onDispose { nestedScrollInterop.stop() }
    }

    val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    val hasSelectionChange = selectedClock != initialClock ||
        selectedSize != initialSize ||
        selectedColorMode != initialColorMode ||
        selectedCustomColor != initialCustomColor
    val previewColor = resolvePreviewClockColor(
        context,
        selectedColorMode,
        selectedCustomColor
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = navBarBottom + 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollInterop)
        ) {
            item(
                key = "clock_customization",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                ClockCustomizationCard(
                    selectedClock = selectedClock,
                    clockSize = selectedSize,
                    colorMode = selectedColorMode,
                    customColor = selectedCustomColor,
                    onClockSizeChange = { selectedSize = it },
                    onColorModeChange = { selectedColorMode = it },
                    onCustomColorChange = { selectedCustomColor = it }
                )
            }

            items(
                count = clockLayouts.size,
                key = { it }
            ) { index ->
                ClockItem(
                    styleIndex = index,
                    name = clockNames[index],
                    layoutRes = clockLayouts[index],
                    isSelected = index == selectedClock,
                    wallpaper = wallpaperBitmap,
                    clockSize = selectedSize,
                    previewColor = previewColor,
                    onClick = { selectedClock = index }
                )
            }
        }

        ExtendedFloatingActionButton(
            onClick = {
                onApply(
                    selectedClock,
                    selectedSize,
                    selectedColorMode,
                    selectedCustomColor
                )
            },
            icon = { Icon(Icons.Default.Check, contentDescription = null) },
            text = { Text(stringResource(R.string.apply)) },
            containerColor = if (hasSelectionChange) {
                FloatingActionButtonDefaults.containerColor
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(16.dp)
        )
    }
}

@Composable
private fun ClockCustomizationCard(
    selectedClock: Int,
    clockSize: Int,
    colorMode: String,
    customColor: Int,
    onClockSizeChange: (Int) -> Unit,
    onColorModeChange: (String) -> Unit,
    onCustomColorChange: (Int) -> Unit
) {
    var showColorDialog by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.valkyos_clock_customization_title),
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.valkyos_clock_color_title),
                style = MaterialTheme.typography.labelLarge
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                ColorModeChip(
                    selected = colorMode == ClockUtils.COLOR_MODE_DEFAULT,
                    text = stringResource(R.string.valkyos_clock_color_default),
                    onClick = { onColorModeChange(ClockUtils.COLOR_MODE_DEFAULT) }
                )
                ColorModeChip(
                    selected = colorMode == ClockUtils.COLOR_MODE_ACCENT,
                    text = stringResource(R.string.valkyos_clock_color_accent),
                    onClick = { onColorModeChange(ClockUtils.COLOR_MODE_ACCENT) }
                )
                ColorModeChip(
                    selected = colorMode == ClockUtils.COLOR_MODE_CUSTOM,
                    text = stringResource(R.string.valkyos_clock_color_custom),
                    onClick = { onColorModeChange(ClockUtils.COLOR_MODE_CUSTOM) }
                )
            }

            if (colorMode == ClockUtils.COLOR_MODE_CUSTOM) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(vertical = 10.dp)
                ) {
                    PRESET_CLOCK_COLORS.forEach { color ->
                        Surface(
                            color = Color(color),
                            shape = CircleShape,
                            border = BorderStroke(
                                if (color == customColor) 3.dp else 1.dp,
                                if (color == customColor) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outline
                                }
                            ),
                            modifier = Modifier
                                .size(36.dp)
                                .clickable { onCustomColorChange(color) }
                        ) {}
                    }
                }
                OutlinedButton(onClick = { showColorDialog = true }) {
                    Text(stringResource(R.string.valkyos_clock_choose_color))
                }
            }

            if (selectedClock == 0) {
                Text(
                    text = stringResource(R.string.valkyos_clock_default_style_info),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else if (ClockUtils.NO_COLOR_CLOCKS.contains(selectedClock)) {
                Text(
                    text = stringResource(R.string.valkyos_clock_no_custom_color),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.valkyos_clock_size_value, clockSize),
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = clockSize.toFloat(),
                onValueChange = {
                    val rounded = (it / CLOCK_SIZE_STEP).roundToInt() * CLOCK_SIZE_STEP
                    onClockSizeChange(
                        rounded.coerceIn(
                            ClockUtils.MIN_CLOCK_SIZE,
                            ClockUtils.MAX_CLOCK_SIZE
                        )
                    )
                },
                valueRange = ClockUtils.MIN_CLOCK_SIZE.toFloat()..
                    ClockUtils.MAX_CLOCK_SIZE.toFloat(),
                steps = 19
            )
        }
    }

    if (showColorDialog) {
        ClockColorHexDialog(
            currentColor = customColor,
            onDismiss = { showColorDialog = false },
            onColorSelected = {
                onCustomColorChange(it)
                showColorDialog = false
            }
        )
    }
}

@Composable
private fun ColorModeChip(
    selected: Boolean,
    text: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
private fun ClockColorHexDialog(
    currentColor: Int,
    onDismiss: () -> Unit,
    onColorSelected: (Int) -> Unit
) {
    var colorText by remember(currentColor) {
        mutableStateOf(String.format("#%06X", currentColor and 0x00FFFFFF))
    }
    var invalid by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.valkyos_clock_choose_color)) },
        text = {
            OutlinedTextField(
                value = colorText,
                onValueChange = {
                    colorText = it
                    invalid = false
                },
                label = { Text(stringResource(R.string.valkyos_clock_color_hex_label)) },
                isError = invalid,
                supportingText = if (invalid) {
                    { Text(stringResource(R.string.valkyos_clock_color_invalid)) }
                } else null,
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = runCatching { AndroidColor.parseColor(colorText.trim()) }.getOrNull()
                    if (parsed == null) {
                        invalid = true
                    } else {
                        onColorSelected(parsed or AndroidColor.BLACK)
                    }
                }
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun ClockItem(
    styleIndex: Int,
    name: String,
    layoutRes: Int,
    isSelected: Boolean,
    wallpaper: ImageBitmap?,
    clockSize: Int,
    previewColor: Int,
    onClick: () -> Unit
) {
    val originalTextColors = remember(layoutRes) { WeakHashMap<android.widget.TextView, Int>() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (wallpaper != null) {
                    Image(
                        bitmap = wallpaper,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = 0.5f
                    )
                }

                AndroidView(
                    factory = { ctx ->
                        FrameLayout(ctx).apply {
                            clipChildren = false
                            clipToPadding = false
                            isClickable = false
                            isFocusable = false
                            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS

                            LayoutInflater.from(ctx).inflate(layoutRes, this, true)

                            if (childCount > 0) {
                                val child = getChildAt(0)
                                captureOriginalTextColors(child, originalTextColors)
                            }
                        }
                    },
                    update = { frame ->
                        if (frame.childCount > 0) {
                            val child = frame.getChildAt(0)
                            val scale = if (styleIndex == 0) {
                                0.45f
                            } else {
                                0.45f * clockSize / ClockUtils.DEFAULT_CLOCK_SIZE
                            }
                            child.scaleX = scale
                            child.scaleY = scale
                            applyPreviewTextColor(
                                child,
                                originalTextColors,
                                if (styleIndex == 0 ||
                                    ClockUtils.NO_COLOR_CLOCKS.contains(styleIndex)
                                ) {
                                    null
                                } else {
                                    previewColor
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                if (isSelected) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .size(22.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                thickness = 0.5.dp
            )

            Text(
                text = name,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(horizontal = 6.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun captureOriginalTextColors(
    view: View,
    colors: WeakHashMap<android.widget.TextView, Int>
) {
    if (view is android.widget.TextView && !colors.containsKey(view)) {
        colors[view] = view.currentTextColor
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            captureOriginalTextColors(view.getChildAt(i), colors)
        }
    }
}

private fun applyPreviewTextColor(
    view: View,
    colors: WeakHashMap<android.widget.TextView, Int>,
    previewColor: Int?
) {
    if (view is android.widget.TextView) {
        val original = colors[view] ?: view.currentTextColor.also { colors[view] = it }
        val originalIsWhite = (original and 0x00FFFFFF) == 0x00FFFFFF
        view.setTextColor(if (previewColor != null && originalIsWhite) previewColor else original)
    }
    if (view is ViewGroup) {
        for (i in 0 until view.childCount) {
            applyPreviewTextColor(view.getChildAt(i), colors, previewColor)
        }
    }
}

private fun resolvePreviewClockColor(
    context: android.content.Context,
    colorMode: String,
    customColor: Int
): Int = when (colorMode) {
    ClockUtils.COLOR_MODE_ACCENT -> {
        val id = context.resources.getIdentifier("system_accent1_100", "color", "android")
        if (id != 0) context.getColor(id) else AndroidColor.WHITE
    }
    ClockUtils.COLOR_MODE_CUSTOM -> customColor
    else -> AndroidColor.WHITE
}

private const val CLOCK_SIZE_STEP = 5

private val PRESET_CLOCK_COLORS = intArrayOf(
    0xFFFFFFFF.toInt(),
    0xFFFF5252.toInt(),
    0xFFFF9800.toInt(),
    0xFFFFD740.toInt(),
    0xFF66BB6A.toInt(),
    0xFF26C6DA.toInt(),
    0xFF448AFF.toInt(),
    0xFF7C4DFF.toInt(),
    0xFFEC407A.toInt()
)

private class ComposeToViewNestedScrollBridge(
    view: View
) : NestedScrollConnection {

    private val helper = NestedScrollingChildHelper(view).apply {
        isNestedScrollingEnabled = true
    }

    private val consumedScroll = IntArray(2)

    private fun NestedScrollSource.toViewType(): Int =
        if (this == NestedScrollSource.Fling) ViewCompat.TYPE_NON_TOUCH
        else ViewCompat.TYPE_TOUCH

    private fun ensureStarted(type: Int) {
        if (!helper.hasNestedScrollingParent(type)) {
            helper.startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL, type)
        }
    }

    private fun toViewDelta(value: Float): Int = (-value).roundToInt()
    private fun toComposeDelta(value: Int): Float = -value.toFloat()

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val type = source.toViewType()
        ensureStarted(type)
        consumedScroll[0] = 0
        consumedScroll[1] = 0
        helper.dispatchNestedPreScroll(
            toViewDelta(available.x),
            toViewDelta(available.y),
            consumedScroll,
            /* offsetInWindow = */ null,
            type
        )
        return Offset(
            toComposeDelta(consumedScroll[0]),
            toComposeDelta(consumedScroll[1])
        )
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset {
        val type = source.toViewType()
        ensureStarted(type)
        consumedScroll[0] = 0
        consumedScroll[1] = 0
        helper.dispatchNestedScroll(
            toViewDelta(consumed.x),
            toViewDelta(consumed.y),
            toViewDelta(available.x),
            toViewDelta(available.y),
            /* offsetInWindow = */ null,
            type,
            consumedScroll
        )
        return Offset(
            toComposeDelta(consumedScroll[0]),
            toComposeDelta(consumedScroll[1])
        )
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        val consumed = helper.dispatchNestedPreFling(-available.x, -available.y)
        return if (consumed) available else Velocity.Zero
    }

    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
        helper.dispatchNestedFling(-available.x, -available.y, /* consumed = */ true)
        return available
    }

    fun stop() {
        helper.stopNestedScroll(ViewCompat.TYPE_TOUCH)
        helper.stopNestedScroll(ViewCompat.TYPE_NON_TOUCH)
    }
}
