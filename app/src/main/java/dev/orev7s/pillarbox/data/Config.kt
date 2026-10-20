package dev.orev7s.pillarbox.data

import org.json.JSONObject

data class Config(val enabled: Boolean, val apps: Map<String, String>) {

    fun toJson(): String {
        val root = JSONObject()
        root.put("enabled", enabled)
        val entries = JSONObject()
        apps.toSortedMap().forEach { (pkg, ratio) -> entries.put(pkg, ratio) }
        root.put("apps", entries)
        return root.toString()
    }

    companion object {
        val EMPTY = Config(false, emptyMap())

        fun parse(raw: String): Config {
            if (raw.isBlank()) return EMPTY
            return try {
                val root = JSONObject(raw)
                val apps = linkedMapOf<String, String>()
                val entries = root.optJSONObject("apps")
                if (entries != null) {
                    val keys = entries.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        val value = entries.optString(key)
                        if (key.isNotEmpty() && value.matches(Regex("^\\d{1,2}:\\d{1,2}$"))) {
                            apps[key] = value
                        }
                    }
                }
                Config(root.optBoolean("enabled", false), apps)
            } catch (t: Throwable) {
                EMPTY
            }
        }
    }
}

object Ratios {
    const val DEFAULT = "4:3"

    data class Option(val value: String, val title: String, val note: String)

    val ALL = listOf(
        Option("4:3", "4:3", "iPad framing, widest vertical FOV"),
        Option("16:10", "16:10", "Mild crop, keeps menus compact"),
        Option("16:9", "16:9", "Phone-style widescreen"),
        Option("5:4", "5:4", "Tall, old tablet framing"),
        Option("3:2", "3:2", "Matches a 3:2 panel exactly"),
        Option("1:1", "1:1", "Square viewport")
    )

    fun title(value: String): String = ALL.firstOrNull { it.value == value }?.title ?: value

    fun aspect(value: String): Float {
        val parts = value.split(":")
        val w = parts.getOrNull(0)?.toFloatOrNull() ?: 4f
        val h = parts.getOrNull(1)?.toFloatOrNull() ?: 3f
        return if (h == 0f) 1.333f else w / h
    }
}
