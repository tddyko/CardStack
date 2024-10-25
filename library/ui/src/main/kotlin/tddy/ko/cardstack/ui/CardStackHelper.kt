package tddy.ko.cardstack.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.Velocity
import kotlin.math.absoluteValue

enum class DragDirection { LEFT, RIGHT, UP, DOWN }

enum class CardAlignment { BOTTOM, BOTTOM_START, BOTTOM_END, TOP, TOP_START, TOP_END, START, END }

enum class DragAlignment { VERTICAL, HORIZONTAL, NONE }

data class CardItem(val id: Int, val description: String, val color: Color)

internal fun determineSwipeDirection(velocity: Velocity, offset: Offset): DragDirection? {
    val isHorizontal = velocity.x.absoluteValue > velocity.y.absoluteValue
    return when {
        isHorizontal && velocity.x > 0 && offset.x > 0 -> DragDirection.RIGHT
        isHorizontal && velocity.x < 0 && offset.x < 0 -> DragDirection.LEFT
        !isHorizontal && velocity.y > 0 && offset.y > 0 -> DragDirection.DOWN
        !isHorizontal && velocity.y < 0 && offset.y < 0 -> DragDirection.UP
        else -> null
    }
}

internal fun isSwipeVelocityExceeded(velocity: Velocity, velocityThresholdPx: Float): Boolean =
    velocity.x.absoluteValue > velocityThresholdPx || velocity.y.absoluteValue > velocityThresholdPx

internal fun calculateTargetOffset(
    direction: DragDirection,
    size: IntSize,
    offset: Offset,
    cardAlignment: CardAlignment
): Offset = when (direction) {
    DragDirection.LEFT -> Offset(-size.width.toFloat() * 1.5f, offset.y)
    DragDirection.RIGHT -> Offset(size.width.toFloat() * 1.5f, offset.y)
    DragDirection.UP -> Offset(offset.x, calculateVerticalOffset(cardAlignment, size, offset))
    DragDirection.DOWN -> Offset(offset.x, size.height.toFloat() * 1.5f)
}

internal fun calculateVerticalOffset(
    cardAlignment: CardAlignment,
    size: IntSize,
    offset: Offset
): Float = when (cardAlignment) {
    CardAlignment.BOTTOM, CardAlignment.BOTTOM_START, CardAlignment.BOTTOM_END -> -size.height.toFloat() * 1.5f
    else -> offset.y
}

internal fun calculateSwipeProgress(offset: Offset, velocityThresholdPx: Float): Float {
    val xProgress = offset.x / velocityThresholdPx
    val yProgress = offset.y / velocityThresholdPx
    return when {
        xProgress.absoluteValue > yProgress.absoluteValue -> xProgress.coerceIn(-1f, 1f)
        else -> -yProgress.coerceIn(-1f, 1f)
    }
}

internal fun calculateScales(index: Int, itemCount: Int, stackDragProgress: Float): Float {
    val baseScale = 1f - (index * 0.05f)
    val scaleIncrement = 0.05f * stackDragProgress
    return when {
        index > 0 -> baseScale + (scaleIncrement * (itemCount - index - 1))
            .coerceAtMost(.05f)

        else -> baseScale
    }
}


internal fun calculateLastOffset(
    cardAlignment: CardAlignment,
    index: Int,
    spacingPx: Float
): Offset = when (cardAlignment) {
    CardAlignment.TOP -> Offset(0f, spacingPx * index)
    CardAlignment.TOP_START -> Offset(-spacingPx * index, spacingPx * index)
    CardAlignment.TOP_END -> Offset(spacingPx * index, spacingPx * index)
    CardAlignment.BOTTOM -> Offset(0f, -spacingPx * index)
    CardAlignment.BOTTOM_START -> Offset(-spacingPx * index, -spacingPx * index)
    CardAlignment.BOTTOM_END -> Offset(spacingPx * index, -spacingPx * index)
    CardAlignment.START -> Offset(-spacingPx * index, 0f)
    CardAlignment.END -> Offset(spacingPx * index, 0f)
}

internal fun calculateDragBasedOffset(
    stackDragProgress: Float,
    index: Int,
    spacingPx: Float,
    itemCount: Int,
    cardAlignment: CardAlignment
): Offset = when {
    index == itemCount - 1 && stackDragProgress < 0f -> {
        val progress = stackDragProgress * (itemCount - index)
        when (cardAlignment) {
            CardAlignment.TOP -> Offset(0f, -(spacingPx * progress) * progress)
            CardAlignment.BOTTOM -> Offset(0f, (spacingPx * progress) * progress)
            CardAlignment.TOP_START, CardAlignment.BOTTOM_START ->
                Offset(
                    (spacingPx * progress) * progress,
                    (spacingPx * progress) * progress * if (cardAlignment == CardAlignment.BOTTOM_START) 1 else -1
                )

            CardAlignment.TOP_END, CardAlignment.BOTTOM_END ->
                Offset(
                    -(spacingPx * progress) * progress,
                    (spacingPx * progress) * progress * if (cardAlignment == CardAlignment.BOTTOM_END) 1 else -1
                )

            CardAlignment.START -> Offset((spacingPx * progress) * progress, 0f)
            CardAlignment.END -> Offset(-(spacingPx * progress) * progress, 0f)
        }
    }

    index > 0 && stackDragProgress != 0f -> {
        val progress = (stackDragProgress * (itemCount - index)).coerceIn(0f, 1f)
        when (cardAlignment) {
            CardAlignment.TOP -> Offset(0f, -spacingPx * progress)
            CardAlignment.BOTTOM -> Offset(0f, spacingPx * progress)
            CardAlignment.TOP_START, CardAlignment.BOTTOM_START ->
                Offset(
                    spacingPx * progress,
                    spacingPx * progress * if (cardAlignment == CardAlignment.BOTTOM_START) 1 else -1
                )

            CardAlignment.TOP_END, CardAlignment.BOTTOM_END ->
                Offset(
                    -spacingPx * progress,
                    spacingPx * progress * if (cardAlignment == CardAlignment.BOTTOM_END) 1 else -1
                )

            CardAlignment.START -> Offset(spacingPx * progress, 0f)
            CardAlignment.END -> Offset(-spacingPx * progress, 0f)
        }
    }

    else -> Offset.Zero
}

internal fun GraphicsLayerScope.applyAlignmentAdjustment(
    cardAlignment: CardAlignment,
    animatedScale: Float,
    size: Size
) {
    val widthAdjustment = (size.width * (1f - animatedScale)) / 2f
    val heightAdjustment = (size.height * (1f - animatedScale)) / 2f

    when (cardAlignment) {
        CardAlignment.TOP -> translationY += heightAdjustment
        CardAlignment.TOP_START -> {
            translationY += heightAdjustment
            translationX -= widthAdjustment
        }

        CardAlignment.TOP_END -> {
            translationY += heightAdjustment
            translationX += widthAdjustment
        }

        CardAlignment.BOTTOM -> translationY -= heightAdjustment
        CardAlignment.BOTTOM_START -> {
            translationY -= heightAdjustment
            translationX -= widthAdjustment
        }

        CardAlignment.BOTTOM_END -> {
            translationY -= heightAdjustment
            translationX += widthAdjustment
        }

        CardAlignment.START -> translationX -= widthAdjustment
        CardAlignment.END -> translationX += widthAdjustment
    }
}