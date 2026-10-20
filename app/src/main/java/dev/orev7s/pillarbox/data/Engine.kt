package dev.orev7s.pillarbox.data

import android.content.Context
import android.util.Base64
import java.io.File

object Engine {

    const val MODULE_DIR = "/data/adb/pillarbox"
    const val FALLBACK_DIR = "/data/local/tmp/pillarbox"
    const val ENGINE_REV = "4"

    data class Snapshot(
        val dir: String,
        val moduleInstalled: Boolean,
        val running: Boolean,
        val config: Config
    )

    private const val PROBE = """
D=/data/adb/pillarbox
[ -d "${'$'}D" ] || D=/data/local/tmp/pillarbox
echo "DIR=${'$'}D"
if [ -f /data/adb/modules/pillarbox/module.prop ]; then echo "MODULE=1"; else echo "MODULE=0"; fi
S=${'$'}("${'$'}D/pillarboxd" status 2>/dev/null | head -n1)
echo "STATUS=${'$'}S"
echo "CONFIG_BEGIN"
cat "${'$'}D/config.json" 2>/dev/null
echo ""
echo "CONFIG_END"
"""

    fun snapshot(): Snapshot {
        val out = Bridge.sh(PROBE.trimIndent(), 25).output
        val dir = out.lineSequence().firstOrNull { it.startsWith("DIR=") }?.removePrefix("DIR=")?.trim()
            ?.takeIf { it.startsWith("/") } ?: FALLBACK_DIR
        val module = out.lineSequence().any { it.trim() == "MODULE=1" }
        val running = out.lineSequence().firstOrNull { it.startsWith("STATUS=") }
            ?.removePrefix("STATUS=")?.trim()?.startsWith("running") == true
        val begin = out.indexOf("CONFIG_BEGIN")
        val end = out.indexOf("CONFIG_END")
        val raw = if (begin >= 0 && end > begin) {
            out.substring(begin + "CONFIG_BEGIN".length, end).trim()
        } else {
            ""
        }
        return Snapshot(dir, module, running, Config.parse(raw))
    }

    fun stageDaemon(context: Context): String {
        val file = File(context.cacheDir, "pillarboxd")
        context.assets.open("engine/pillarboxd").use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        return file.absolutePath
    }

    fun ensure(context: Context, dir: String): Boolean {
        val staged = stageDaemon(context)
        val cmd = """
D="$dir"
mkdir -p "${'$'}D"
if [ "${'$'}(cat "${'$'}D/.engine" 2>/dev/null)" != "$ENGINE_REV" ]; then
  cp "$staged" "${'$'}D/pillarboxd" && chmod 755 "${'$'}D/pillarboxd" && echo $ENGINE_REV > "${'$'}D/.engine"
fi
[ -f "${'$'}D/config.json" ] || printf '%s' '{"enabled":false,"apps":{}}' > "${'$'}D/config.json"
chmod 644 "${'$'}D/config.json"
echo READY
""".trimIndent()
        return Bridge.sh(cmd, 25).output.contains("READY")
    }

    fun saveConfig(dir: String, config: Config): Boolean {
        val encoded = Base64.encodeToString(config.toJson().toByteArray(), Base64.NO_WRAP)
        val cmd = "echo $encoded | base64 -d > \"$dir/config.json\" && chmod 644 \"$dir/config.json\" && echo OK"
        return Bridge.sh(cmd, 20).output.contains("OK")
    }

    fun start(dir: String): Boolean =
        Bridge.sh("\"$dir/pillarboxd\" start", 25).output.let {
            it.contains("started") || it.contains("already")
        }

    fun stop(dir: String): Boolean =
        Bridge.sh("\"$dir/pillarboxd\" stop", 20).output.contains("stopped")

    fun resetDisplay(dir: String): Boolean =
        Bridge.sh("\"$dir/pillarboxd\" stop", 20).output.contains("stopped")

    fun rebootHint(): Boolean =
        Bridge.sh("getprop sys.boot_completed", 8).output.trim() == "1"
}
