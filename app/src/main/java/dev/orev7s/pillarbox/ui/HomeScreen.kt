package dev.orev7s.pillarbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.orev7s.pillarbox.Rule
import dev.orev7s.pillarbox.UiState
import dev.orev7s.pillarbox.data.Ratios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: UiState,
    onEngineToggle: (Boolean) -> Unit,
    onAdd: () -> Unit,
    onReset: () -> Unit,
    onRatio: (String, String) -> Unit,
    onRemove: (String) -> Unit
) {
    var sheet by remember { mutableStateOf<Rule?>(null) }

    Scaffold(
        containerColor = Ink,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "PillarBox",
                            fontSize = 21.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextMain
                        )
                        Text(
                            text = "Per-app aspect engine",
                            fontSize = 11.sp,
                            color = TextDim
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onReset) {
                        Icon(Icons.Rounded.RestartAlt, contentDescription = "Reset display", tint = TextDim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 6.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { EngineCard(state, onEngineToggle) }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "APPS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextDim,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = state.rules.size.toString(),
                        fontSize = 11.sp,
                        color = TextDim.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = onAdd) {
                        Icon(Icons.Rounded.Add, contentDescription = null, tint = Accent, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add apps", color = Accent, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (state.rules.isEmpty()) {
                item { EmptyCard(state, onAdd) }
            } else {
                items(state.rules, key = { it.pkg }) { rule ->
                    RuleRow(rule) { sheet = rule }
                }
            }

            item {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = if (state.module) "Magisk module · engine at ${state.dir}" else "Standalone mode · engine at ${state.dir}",
                    fontSize = 10.sp,
                    color = TextDim.copy(alpha = 0.55f)
                )
            }
        }
    }

    sheet?.let { rule ->
        RatioSheet(
            rule = rule,
            onDismiss = { sheet = null },
            onRatio = { ratio ->
                onRatio(rule.pkg, ratio)
                sheet = null
            },
            onRemove = {
                onRemove(rule.pkg)
                sheet = null
            }
        )
    }
}

@Composable
private fun EngineCard(state: UiState, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg)
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(if (state.engineOn) AccentSoft else CardAlt),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Bolt,
                        contentDescription = null,
                        tint = if (state.engineOn) Accent else TextDim,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = when {
                            !state.root -> "Root required"
                            state.engineOn -> "Engine running"
                            else -> "Engine paused"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMain
                    )
                    Text(
                        text = when {
                            !state.root -> "Grant superuser access, then reopen"
                            state.engineOn -> "Switches aspect only for the apps below"
                            else -> "Your display stays untouched"
                        },
                        fontSize = 11.5.sp,
                        color = TextDim
                    )
                }
                Switch(
                    checked = state.engineOn,
                    onCheckedChange = onToggle,
                    enabled = state.root && !state.busy,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0A0C11),
                        checkedTrackColor = Accent,
                        uncheckedThumbColor = TextDim,
                        uncheckedTrackColor = CardAlt,
                        uncheckedBorderColor = Line
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Tag(
                    text = if (state.root) "Root OK" else "No root",
                    color = if (state.root) Mint else Danger,
                    dot = true
                )
                Tag(
                    text = if (state.module) "Module" else "Standalone",
                    color = if (state.module) Accent else Amber
                )
                Tag(
                    text = if (state.running) "Daemon live" else "Daemon idle",
                    color = if (state.running) Mint else TextDim,
                    dot = true
                )
            }
        }
    }
}

@Composable
private fun RuleRow(rule: Rule, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardAlt)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppAvatar(rule.icon, rule.label, 38)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = rule.label,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMain,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = rule.pkg,
                    fontSize = 10.5.sp,
                    color = TextDim.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(10.dp))
            RatioGlyph(Ratios.aspect(rule.ratio), true)
            Spacer(Modifier.width(8.dp))
            Text(
                text = rule.ratio,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Accent
            )
        }
    }
}

@Composable
private fun EmptyCard(state: UiState, onAdd: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardAlt)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                text = "No apps configured",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextMain
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Pick the games you want framed at 4:3. Everything else keeps the native panel ratio.",
                fontSize = 12.sp,
                color = TextDim,
                lineHeight = 17.sp
            )
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = onAdd,
                enabled = state.root,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Color(0xFF0A0C11))
            ) {
                Text("Choose apps", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RatioSheet(
    rule: Rule,
    onDismiss: () -> Unit,
    onRatio: (String) -> Unit,
    onRemove: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        contentColor = TextMain
    ) {
        Column(Modifier.padding(horizontal = 18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppAvatar(rule.icon, rule.label, 44)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = rule.label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextMain,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = rule.pkg,
                        fontSize = 11.sp,
                        color = TextDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Text(
                text = "ASPECT RATIO",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextDim,
                letterSpacing = 1.1.sp
            )
            Spacer(Modifier.height(6.dp))

            Ratios.ALL.forEach { option ->
                val selected = option.value == rule.ratio
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onRatio(option.value) }
                        .padding(horizontal = 8.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatioGlyph(Ratios.aspect(option.value), selected)
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = option.title,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) TextMain else TextMain.copy(alpha = 0.85f)
                        )
                        Text(
                            text = option.note,
                            fontSize = 11.sp,
                            color = TextDim
                        )
                    }
                    if (selected) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Accent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onRemove,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Remove from list", color = Danger, fontSize = 13.sp)
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}
