package dev.orev7s.pillarbox.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

@Composable
fun AppAvatar(icon: ImageBitmap?, label: String, size: Int = 40) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(RoundedCornerShape((size * 0.28f).dp))
            .background(CardAlt),
        contentAlignment = Alignment.Center
    ) {
        if (icon != null) {
            Image(
                bitmap = icon,
                contentDescription = label,
                modifier = Modifier.size(size.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            Text(
                text = label.take(1).uppercase(),
                color = TextDim,
                fontSize = (size * 0.4f).sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun RatioGlyph(
    aspect: Float,
    active: Boolean,
    modifier: Modifier = Modifier.size(38.dp, 24.dp)
) {
    Canvas(modifier = modifier) {
        val radius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        drawRoundRect(
            color = Line,
            cornerRadius = radius,
            style = Stroke(width = 1.4.dp.toPx())
        )

        val panel = 1.5f
        val innerW = if (aspect >= panel) size.width else size.height * aspect
        val innerH = if (aspect >= panel) size.width / aspect else size.height
        val left = (size.width - innerW) / 2f
        val top = (size.height - innerH) / 2f

        drawRoundRect(
            color = if (active) Accent.copy(alpha = 0.9f) else TextDim.copy(alpha = 0.35f),
            topLeft = Offset(left, top),
            size = Size(innerW, innerH),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun Tag(text: String, color: Color, dot: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (dot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
                Spacer(Modifier.size(6.dp))
            }
            Text(
                text = text,
                color = color,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
