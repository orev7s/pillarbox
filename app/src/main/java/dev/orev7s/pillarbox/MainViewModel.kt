package dev.orev7s.pillarbox

import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.orev7s.pillarbox.data.AppEntry
import dev.orev7s.pillarbox.data.AppInventory
import dev.orev7s.pillarbox.data.Bridge
import dev.orev7s.pillarbox.data.Config
import dev.orev7s.pillarbox.data.Engine
import dev.orev7s.pillarbox.data.Ratios
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class Rule(
    val pkg: String,
    val label: String,
    val ratio: String,
    val icon: ImageBitmap?
)

data class UiState(
    val loading: Boolean = true,
    val root: Boolean = false,
    val module: Boolean = false,
    val engineOn: Boolean = false,
    val running: Boolean = false,
    val dir: String = Engine.FALLBACK_DIR,
    val rules: List<Rule> = emptyList(),
    val apps: List<AppEntry> = emptyList(),
    val busy: Boolean = false,
    val error: String? = null
)

private data class Loaded(
    val module: Boolean,
    val config: Config,
    val running: Boolean
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    private var config: Config = Config.EMPTY
    private var dir: String = Engine.FALLBACK_DIR
    private var inventory: List<AppEntry> = emptyList()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            val loaded: Loaded? = withContext(Dispatchers.IO) {
                if (!Bridge.rootAvailable()) {
                    null
                } else {
                    val snap = Engine.snapshot()
                    dir = snap.dir
                    Engine.ensure(getApplication(), snap.dir)
                    if (snap.config.enabled && !snap.running) Engine.start(snap.dir)
                    if (inventory.isEmpty()) inventory = AppInventory.load(getApplication())
                    Loaded(snap.moduleInstalled, snap.config, snap.running || snap.config.enabled)
                }
            }
            if (loaded == null) {
                _state.update {
                    it.copy(loading = false, root = false, error = "Root access is required")
                }
                return@launch
            }
            config = loaded.config
            rebuild(module = loaded.module, running = loaded.running, loading = false)
        }
    }

    fun setEngine(enabled: Boolean) {
        if (_state.value.busy) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            val ok = withContext(Dispatchers.IO) {
                config = config.copy(enabled = enabled)
                Engine.saveConfig(dir, config) && if (enabled) Engine.start(dir) else Engine.stop(dir)
            }
            _state.update {
                it.copy(
                    busy = false,
                    engineOn = enabled && ok,
                    running = enabled && ok,
                    error = if (ok) null else "Could not reach the engine"
                )
            }
            if (!ok) refresh()
        }
    }

    fun addApps(packages: Set<String>, ratio: String) {
        if (packages.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            val updated = config.copy(apps = config.apps + packages.associateWith { ratio }, enabled = true)
            val ok = withContext(Dispatchers.IO) {
                config = updated
                Engine.saveConfig(dir, config) && Engine.start(dir)
            }
            _state.update { it.copy(busy = false, engineOn = ok, running = ok) }
            rebuild(module = _state.value.module, running = ok, loading = false)
        }
    }

    fun setRatio(pkg: String, ratio: String) {
        viewModelScope.launch {
            val updated = config.copy(apps = config.apps + (pkg to ratio))
            val ok = withContext(Dispatchers.IO) {
                config = updated
                Engine.saveConfig(dir, config) && Engine.start(dir)
            }
            _state.update { it.copy(engineOn = ok || it.engineOn) }
            rebuild(module = _state.value.module, running = _state.value.running, loading = false)
        }
    }

    fun removeApp(pkg: String) {
        viewModelScope.launch {
            val updated = config.copy(apps = config.apps - pkg)
            withContext(Dispatchers.IO) {
                config = updated
                Engine.saveConfig(dir, config)
            }
            rebuild(module = _state.value.module, running = _state.value.running, loading = false)
        }
    }

    fun resetDisplay() {
        viewModelScope.launch {
            _state.update { it.copy(busy = true) }
            withContext(Dispatchers.IO) { Engine.resetDisplay(dir) }
            config = config.copy(enabled = false)
            withContext(Dispatchers.IO) { Engine.saveConfig(dir, config) }
            _state.update { it.copy(busy = false, engineOn = false, running = false) }
            rebuild(module = _state.value.module, running = false, loading = false)
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun rebuild(module: Boolean, running: Boolean, loading: Boolean) {
        val index = inventory.associateBy { it.pkg }
        val rules = config.apps.map { (pkg, ratio) ->
            Rule(
                pkg = pkg,
                label = index[pkg]?.label ?: pkg,
                ratio = ratio,
                icon = index[pkg]?.icon
            )
        }.sortedWith(compareBy({ Ratios.ALL.indexOfFirst { o -> o.value == it.ratio } }, { it.label.lowercase() }))

        _state.update {
            it.copy(
                loading = loading,
                root = true,
                module = module,
                engineOn = config.enabled,
                running = running,
                dir = dir,
                rules = rules,
                apps = inventory,
                error = null
            )
        }
    }
}
