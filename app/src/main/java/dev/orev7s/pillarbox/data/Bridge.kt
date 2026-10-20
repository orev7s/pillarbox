package dev.orev7s.pillarbox.data

import java.util.concurrent.TimeUnit

object Bridge {

    data class Result(val code: Int, val output: String)

    fun sh(cmd: String, timeoutSec: Long = 20): Result {
        val process = try {
            ProcessBuilder("su", "-c", cmd).redirectErrorStream(true).start()
        } catch (t: Throwable) {
            return Result(-1, t.message ?: "su unavailable")
        }

        try {
            process.outputStream.close()
        } catch (_: Throwable) {
        }

        val buffer = StringBuilder()
        val reader = Thread {
            try {
                process.inputStream.bufferedReader().forEachLine { buffer.append(it).append('\n') }
            } catch (_: Throwable) {
            }
        }
        reader.isDaemon = true
        reader.start()

        val finished = try {
            process.waitFor(timeoutSec, TimeUnit.SECONDS)
        } catch (t: Throwable) {
            false
        }
        if (!finished) process.destroyForcibly()
        reader.join(800)

        val code = if (finished) process.exitValue() else -1
        return Result(code, buffer.toString().trim())
    }

    fun rootAvailable(): Boolean = sh("id -u", 12).output.trim() == "0"
}
