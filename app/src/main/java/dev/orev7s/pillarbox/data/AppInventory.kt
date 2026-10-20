package dev.orev7s.pillarbox.data

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: ImageBitmap?,
    val system: Boolean
)

object AppInventory {

    private const val ICON_PX = 112

    fun load(context: Context): List<AppEntry> {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolved = pm.queryIntentActivities(intent, 0)
        val seen = HashSet<String>()
        val result = ArrayList<AppEntry>(resolved.size)

        for (info in resolved) {
            val pkg = info.activityInfo?.packageName ?: continue
            if (pkg == context.packageName) continue
            if (!seen.add(pkg)) continue
            val app = info.activityInfo.applicationInfo
            val label = runCatching { pm.getApplicationLabel(app).toString() }.getOrDefault(pkg)
            val icon = runCatching { pm.getApplicationIcon(app).render(ICON_PX) }.getOrNull()
            val system = (app.flags and ApplicationInfo.FLAG_SYSTEM) != 0 ||
                (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0
            result.add(AppEntry(pkg, label, icon, system))
        }

        return result.sortedWith(compareBy<AppEntry> { it.system }.thenBy { it.label.lowercase() })
    }

    private fun Drawable.render(size: Int): ImageBitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        setBounds(0, 0, size, size)
        draw(canvas)
        return bitmap.asImageBitmap()
    }
}
