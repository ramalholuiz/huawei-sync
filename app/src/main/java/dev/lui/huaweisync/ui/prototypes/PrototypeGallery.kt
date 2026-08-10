package dev.lui.huaweisync.ui.prototypes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.lui.huaweisync.ui.prototypes.conceptA.ConceptADashboard
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

enum class PrototypeConcept(val label: String) {
    A("A · Soft Premium Health"),
    B("B · Dynamic Fitness Utility (WIP)"),
    C("C · Connected Ecosystem (WIP)"),
}

@Composable
fun PrototypeGalleryDialog(onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false,
        ),
    ) {
        PrototypeGalleryRoot(onDismiss = onDismiss)
    }
}

@Composable
private fun PrototypeGalleryRoot(onDismiss: () -> Unit) {
    var concept by rememberSaveable { mutableStateOf(PrototypeConcept.A) }
    var fixture by rememberSaveable { mutableStateOf(PrototypeState.EMPTY) }
    var darkTheme by rememberSaveable { mutableStateOf(true) }

    HuaweiSyncTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HuaweiSyncTheme.colors.canvasBackground)
                .testTag("prototype-gallery-root"),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                GalleryTopBar(
                    darkTheme = darkTheme,
                    onToggleTheme = { darkTheme = !darkTheme },
                    onDismiss = onDismiss,
                )
                ConceptTabs(current = concept, onSelect = { concept = it })
                StateSelector(current = fixture, onSelect = { fixture = it })
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 8.dp)
                        .testTag("prototype-gallery-stage"),
                ) {
                    val syncState = remember(fixture) { PrototypeGalleryFixtures.stateFor(fixture) }
                    when (concept) {
                        PrototypeConcept.A -> ConceptADashboard(
                            sync = syncState,
                            fixture = fixture,
                        )
                        PrototypeConcept.B -> PlaceholderConcept("Concept B lands after Concept A validation.")
                        PrototypeConcept.C -> PlaceholderConcept("Concept C lands after Concept B validation.")
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryTopBar(
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onDismiss: () -> Unit,
) {
    val systemPadding = WindowInsets.systemBars.asPaddingValues()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.surface1)
            .padding(
                top = systemPadding.calculateTopPadding() + 8.dp,
                bottom = 12.dp,
                start = 16.dp,
                end = 16.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Prototype gallery · debug only",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "Cycle 3 · visual language break",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextChip(label = if (darkTheme) "Light" else "Dark", onClick = onToggleTheme)
            Spacer(Modifier.size(8.dp))
            TextChip(label = "Close", onClick = onDismiss, accent = true)
        }
    }
}

@Composable
private fun ConceptTabs(current: PrototypeConcept, onSelect: (PrototypeConcept) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.surface1)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PrototypeConcept.entries.forEach { c ->
            val selected = c == current
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) HuaweiSyncTheme.colors.surface3
                        else HuaweiSyncTheme.colors.surface2,
                    )
                    .clickable { onSelect(c) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = c.name,
                    color = if (selected) HuaweiSyncTheme.colors.ink else HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun StateSelector(current: PrototypeState, onSelect: (PrototypeState) -> Unit) {
    val scroll = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(HuaweiSyncTheme.colors.surface1)
            .horizontalScroll(scroll)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        PrototypeState.entries.forEach { s ->
            val selected = s == current
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        if (selected) HuaweiSyncTheme.colors.accentSoft
                        else HuaweiSyncTheme.colors.surface2,
                    )
                    .border(
                        width = 1.dp,
                        color = if (selected) HuaweiSyncTheme.colors.accent else HuaweiSyncTheme.colors.line,
                        shape = RoundedCornerShape(999.dp),
                    )
                    .clickable { onSelect(s) }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    text = s.label,
                    color = if (selected) HuaweiSyncTheme.colors.ink else HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun TextChip(label: String, onClick: () -> Unit, accent: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (accent) HuaweiSyncTheme.colors.accentContainer
                else HuaweiSyncTheme.colors.surface2,
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
    ) {
        Text(
            text = label,
            color = if (accent) HuaweiSyncTheme.colors.background else HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun PlaceholderConcept(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HuaweiSyncTheme.colors.background)
            .padding(
                horizontal = 24.dp,
                vertical = 40.dp,
            ),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Coming next",
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = message,
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

