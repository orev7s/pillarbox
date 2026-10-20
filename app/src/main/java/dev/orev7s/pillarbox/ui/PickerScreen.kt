package dev.orev7s.pillarbox.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.orev7s.pillarbox.data.AppEntry
import dev.orev7s.pillarbox.data.Ratios

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickerScreen(
    apps: List<AppEntry>,
    configured: Set<String>,
    onBack: () -> Unit,
    onConfirm: (Set<String>, String) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selection by remember { mutableStateOf(setOf<String>()) }
    var ratio by remember { mutableStateOf(Ratios.DEFAULT) }

    val filtered = remember(query, apps) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) apps else apps.filter {
            it.label.lowercase().contains(q) || it.pkg.lowercase().contains(q)
        }
    }

    Scaffold(
        containerColor = Ink,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back", tint = TextMain)
                    }
                },
                title = {
                    Column {
                        Text("Select apps", fontSize = 17.sp, fontWeight = FontWeight.SemiBold, color = TextMain)
                        Text("Only these get the 4:3 frame", fontSize = 11.sp, color = TextDim)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Ink)
            )
        },
        bottomBar = {
            Surface(color = CardBg) {
                Column(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Ratios.ALL.forEach { option ->
                            FilterChip(
                                selected = option.value == ratio,
                                onClick = { ratio = option.value },
                                label = { Text(option.title, fontSize = 12.sp) },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AccentSoft,
                                    selectedLabelColor = Accent,
                                    labelColor = TextDim
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = option.value == ratio,
                                    borderColor = Line
                                )
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onConfirm(selection, ratio) },
                        enabled = selection.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp),
                        shape = RoundedCornerShape(13.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Accent,
                            contentColor = Color(0xFF0A0C11),
                            disabledContainerColor = CardAlt,
                            disabledContentColor = TextDim
                        )
                    ) {
                        Text(
                            text = if (selection.isEmpty()) "Select at least one app" else "Add ${selection.size} app${if (selection.size == 1) "" else "s"}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp),
                placeholder = { Text("Search apps", fontSize = 13.sp, color = TextDim) },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = TextDim, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Rounded.Close, contentDescription = null, tint = TextDim, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {})
            )

            LazyColumn(Modifier.fillMaxSize()) {
                items(filtered, key = { it.pkg }) { app ->
                    val already = app.pkg in configured
                    val checked = already || app.pkg in selection
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !already) {
                                selection = if (app.pkg in selection) selection - app.pkg else selection + app.pkg
                            }
                            .padding(horizontal = 18.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppAvatar(app.icon, app.label, 40)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = app.label,
                                fontSize = 14.sp,
                                color = if (already) TextDim else TextMain,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (already) "Already configured" else app.pkg,
                                fontSize = 10.5.sp,
                                color = TextDim.copy(alpha = 0.75f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Switch(
                            checked = checked,
                            onCheckedChange = {
                                if (!already) {
                                    selection = if (app.pkg in selection) selection - app.pkg else selection + app.pkg
                                }
                            },
                            enabled = !already,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF0A0C11),
                                checkedTrackColor = Accent,
                                uncheckedThumbColor = TextDim,
                                uncheckedTrackColor = CardAlt,
                                uncheckedBorderColor = Line
                            )
                        )
                    }
                }
                if (filtered.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("Nothing matches \"$query\"", fontSize = 13.sp, color = TextDim)
                        }
                    }
                }
            }
        }
    }
}
