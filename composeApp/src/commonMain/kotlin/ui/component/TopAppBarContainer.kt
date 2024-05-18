/*
 * Copyright (c) 2024 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.kotlin.files.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.DecayAnimationSpec
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDecay
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isFinite
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import kotlin.math.abs
import kotlin.math.roundToInt

/** @see androidx.compose.material3.SingleRowTopAppBar */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TopAppBarContainer(
    modifier: Modifier = Modifier,
    expandedHeight: Dp = TopAppBarDefaults.TopAppBarExpandedHeight,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(),
    scrollBehavior: TopAppBarScrollBehavior? = null,
    content: @Composable () -> Unit
) {
    require(expandedHeight.isSpecified && expandedHeight.isFinite) {
        "The expandedHeight is expected to be specified and finite"
    }
    val expandedHeightPx = with(LocalDensity.current) { expandedHeight.toPx().coerceAtLeast(0f) }
    SideEffect {
        // Sets the app bar's height offset to collapse the entire bar's height when content is
        // scrolled.
        if (scrollBehavior?.state?.heightOffsetLimit != -expandedHeightPx) {
            scrollBehavior?.state?.heightOffsetLimit = -expandedHeightPx
        }
    }

    // Obtain the container color from the TopAppBarColors using the `overlapFraction`. This
    // ensures that the colors will adjust whether the app bar behavior is pinned or scrolled.
    // This may potentially animate or interpolate a transition between the container-color and the
    // container's scrolled-color according to the app bar's scroll state.
    val colorTransitionFraction by
        remember(scrollBehavior) {
            // derivedStateOf to prevent redundant recompositions when the content scrolls.
            derivedStateOf {
                val overlappingFraction = scrollBehavior?.state?.overlappedFraction ?: 0f
                if (overlappingFraction > 0.01f) 1f else 0f
            }
        }
    val appBarContainerColor by
        animateColorAsState(
            targetValue = colors.containerColor(colorTransitionFraction),
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )

    // Set up support for resizing the top app bar when vertically dragging the bar itself.
    val appBarDragModifier =
        if (scrollBehavior != null && !scrollBehavior.isPinned) {
            Modifier.draggable(
                orientation = Orientation.Vertical,
                state =
                    rememberDraggableState { delta -> scrollBehavior.state.heightOffset += delta },
                onDragStopped = { velocity ->
                    settleAppBar(
                        scrollBehavior.state,
                        velocity,
                        scrollBehavior.flingAnimationSpec,
                        scrollBehavior.snapAnimationSpec
                    )
                }
            )
        } else {
            Modifier
        }

    // Compose a Surface with a TopAppBarLayout content.
    // The surface's background color is animated as specified above.
    // The height of the app bar is determined by subtracting the bar's height offset from the
    // app bar's defined constant height value (i.e. the ContainerHeight token).
    Surface(
        modifier = modifier.then(appBarDragModifier),
        color = appBarContainerColor
    ) {
        TopAppBarLayout(
            modifier = Modifier
                .windowInsetsPadding(windowInsets)
                // clip after padding so we don't show the title over the inset area
                .clipToBounds()
                .heightIn(max = expandedHeight),
            scrolledOffset = { scrollBehavior?.state?.heightOffset ?: 0f },
            content = content
        )
    }
}

// TODO: Remove when library version is updated.
@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UnusedReceiverParameter")
private val TopAppBarDefaults.TopAppBarExpandedHeight
    get() = 64.dp

@OptIn(ExperimentalMaterial3Api::class)
private fun TopAppBarColors.containerColor(colorTransitionFraction: Float): Color =
    lerp(
        containerColor,
        scrolledContainerColor,
        FastOutLinearInEasing.transform(colorTransitionFraction)
    )

@OptIn(ExperimentalMaterial3Api::class)
private suspend fun settleAppBar(
    state: TopAppBarState,
    velocity: Float,
    flingAnimationSpec: DecayAnimationSpec<Float>?,
    snapAnimationSpec: AnimationSpec<Float>?
): Velocity {
    // Check if the app bar is completely collapsed/expanded. If so, no need to settle the app bar,
    // and just return Zero Velocity.
    // Note that we don't check for 0f due to float precision with the collapsedFraction
    // calculation.
    if (state.collapsedFraction < 0.01f || state.collapsedFraction == 1f) {
        return Velocity.Zero
    }
    var remainingVelocity = velocity
    // In case there is an initial velocity that was left after a previous user fling, animate to
    // continue the motion to expand or collapse the app bar.
    if (flingAnimationSpec != null && abs(velocity) > 1f) {
        var lastValue = 0f
        AnimationState(
                initialValue = 0f,
                initialVelocity = velocity,
            )
            .animateDecay(flingAnimationSpec) {
                val delta = value - lastValue
                val initialHeightOffset = state.heightOffset
                state.heightOffset = initialHeightOffset + delta
                val consumed = abs(initialHeightOffset - state.heightOffset)
                lastValue = value
                remainingVelocity = this.velocity
                // avoid rounding errors and stop if anything is unconsumed
                if (abs(delta - consumed) > 0.5f) this.cancelAnimation()
            }
    }
    // Snap if animation specs were provided.
    if (snapAnimationSpec != null) {
        if (state.heightOffset < 0 && state.heightOffset > state.heightOffsetLimit) {
            AnimationState(initialValue = state.heightOffset).animateTo(
                if (state.collapsedFraction < 0.5f) {
                    0f
                } else {
                    state.heightOffsetLimit
                },
                animationSpec = snapAnimationSpec
            ) {
                state.heightOffset = value
            }
        }
    }

    return Velocity(0f, remainingVelocity)
}

@Composable
private fun TopAppBarLayout(
    modifier: Modifier,
    scrolledOffset: ScrolledOffset,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.fastMap { it.measure(constraints) }

        // Subtract the scrolledOffset from the maxHeight. The scrolledOffset is expected to be
        // equal or smaller than zero.
        val scrolledOffsetValue = scrolledOffset.offset()
        val heightOffset = if (scrolledOffsetValue.isNaN()) 0 else scrolledOffsetValue.roundToInt()

        val layoutHeight = if (constraints.maxHeight == Constraints.Infinity) {
            constraints.maxHeight
        } else {
            constraints.maxHeight + heightOffset
        }

        layout(constraints.maxWidth, layoutHeight) {
            placeables.fastForEach { it.placeRelative(0, (layoutHeight - it.height) / 2) }
        }
    }
}

private fun interface ScrolledOffset {
    fun offset(): Float
}
