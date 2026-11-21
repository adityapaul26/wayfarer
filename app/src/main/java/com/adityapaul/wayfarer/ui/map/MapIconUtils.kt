package com.adityapaul.wayfarer.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory

object MapIconUtils {

    fun createOriginCircleIcon(context: Context, colorInt: Int = android.graphics.Color.parseColor("#059669")): Icon {
        val sizeDp = 24
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = sizePx / 2f
        val radius = center - 3 * density

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, center - density, borderPaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius, fillPaint)

        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius * 0.35f, corePaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    fun createDestinationPinIcon(context: Context, colorInt: Int = android.graphics.Color.parseColor("#EF4444")): Icon {
        val widthDp = 30
        val heightDp = 40
        val density = context.resources.displayMetrics.density
        val widthPx = (widthDp * density).toInt().coerceAtLeast(1)
        val heightPx = (heightDp * density).toInt().coerceAtLeast(1)

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val radius = widthPx / 2f

        val path = Path().apply {
            moveTo(radius, heightPx.toFloat())
            cubicTo(
                radius * 0.2f, heightPx * 0.65f,
                0f, radius * 1.3f,
                0f, radius
            )
            arcTo(0f, 0f, widthPx.toFloat(), widthPx.toFloat(), 180f, 180f, false)
            cubicTo(
                widthPx.toFloat(), radius * 1.3f,
                radius * 1.8f, heightPx * 0.65f,
                radius, heightPx.toFloat()
            )
            close()
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, strokePaint)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colorInt
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius - 2 * density, fillPaint)

        val tipPath = Path().apply {
            moveTo(radius, heightPx.toFloat() - 2 * density)
            lineTo(radius - radius * 0.3f, radius)
            lineTo(radius + radius * 0.3f, radius)
            close()
        }
        canvas.drawPath(tipPath, fillPaint)

        val targetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius * 0.35f, targetPaint)

        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }
}
