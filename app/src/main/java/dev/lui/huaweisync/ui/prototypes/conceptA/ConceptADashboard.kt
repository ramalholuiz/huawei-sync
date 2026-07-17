package dev.lui.huaweisync.ui.prototypes.conceptA

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.HuaweiSyncMotion
import dev.lui.huaweisync.ui.components.SyncPipelineRail
import dev.lui.huaweisync.ui.components.SyncRailOrientation
import dev.lui.huaweisync.ui.prototypes.PrototypeState
import dev.lui.huaweisync.ui.screens.pipeline.PipelinePresentation
import dev.lui.huaweisync.ui.state.ProductGymRatsStatus
import dev.lui.huaweisync.ui.state.ProductHealthConnectStatus
import dev.lui.huaweisync.ui.state.ProductSyncPhase
import dev.lui.huaweisync.ui.state.ProductSyncState
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme

/**
 * Concept A — Soft Premium Health. Rounded, calm, wellness-forward. Reads the same
 * production `ProductSyncState` fixtures the real Dashboard reads. Local tokens only;
 * no `HuaweiSyncShapes` / `HuaweiSyncGeometry` / production `Modernist*` chrome.
 */
@Composable
fun ConceptADashboard(
    sync: ProductSyncState,
    fixture: PrototypeState,
    modifier: Modifier = Modifier,
) {
    val bg = HuaweiSyncTheme.colors.background
    val systemPadding = WindowInsets.systemBars.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .testTag("concept-a-dashboard"),
    ) {
        ConceptATopBar()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = ConceptATokens.paddingScreenH,
                end = ConceptATokens.paddingScreenH,
                top = ConceptATokens.paddingScreenV,
                bottom = ConceptATokens.paddingScreenV + systemPadding.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(ConceptATokens.gapCards),
        ) {
            item { HeroCard(sync = sync, fixture = fixture) }
            item { LedgerCard(sync = sync) }
            item { PipelineCard(sync = sync) }
            item { GymRatsCard() }
            sync.sanitizedFailureSummary?.let { summary ->
                item { FailureCard(summary = summary) }
            }
        }
    }
}

@Composable
private fun ConceptATopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ConceptATokens.paddingScreenH,
                vertical = ConceptATokens.gapContent,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Today",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = "Your workouts",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(HuaweiSyncTheme.colors.surface2),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "H",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}

@Composable
private fun HeroCard(sync: ProductSyncState, fixture: PrototypeState) {
    val dominant = sync.dominantConceptAState()
    val wash = when (dominant) {
        DominantState.SUCCESS -> ConceptATokens.accentWash()
        DominantState.WAITING, DominantState.SYNCING -> ConceptATokens.waitingWash()
        DominantState.ERROR -> ConceptATokens.errorWash()
        DominantState.READY -> null
    }
    val motion = HuaweiSyncMotion.current
    val baseModifier = Modifier
        .fillMaxWidth()
        .shadow(
            elevation = 1.dp,
            shape = ConceptATokens.heroShape,
            ambientColor = HuaweiSyncTheme.colors.ink.copy(alpha = 0.6f),
            spotColor = HuaweiSyncTheme.colors.accent.copy(alpha = 0.15f),
        )
        .clip(ConceptATokens.heroShape)
        .background(HuaweiSyncTheme.colors.surface1)
    val boxModifier = if (wash != null) {
        baseModifier.background(brush = wash)
    } else {
        baseModifier
    }
    Box(
        modifier = boxModifier
            .padding(ConceptATokens.paddingHeroInner)
            .testTag("concept-a-hero"),
    ) {
        AnimatedContent(
            targetState = dominant,
            transitionSpec = {
                if (motion.reducedMotion) {
                    fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                } else {
                    fadeIn(tween(motion.standardMillis)) togetherWith
                        fadeOut(tween(motion.fastMillis))
                }
            },
            label = "concept-a-hero-content",
        ) { state ->
            HeroContent(state = state, sync = sync, fixture = fixture)
        }
    }
}

@Composable
private fun HeroContent(
    state: DominantState,
    sync: ProductSyncState,
    fixture: PrototypeState,
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StateDot(state = state)
            Spacer(Modifier.size(ConceptATokens.gapContent))
            Text(
                text = state.eyebrow(),
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Spacer(Modifier.height(ConceptATokens.gapContent))
        Text(
            text = when (state) {
                DominantState.READY -> "Ready to sync your first workout"
                DominantState.WAITING -> "Waiting to sync"
                DominantState.SYNCING -> "Syncing your workout"
                DominantState.SUCCESS -> "Workout confirmed in Health Connect"
                DominantState.ERROR -> "Sync couldn't complete"
            },
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = when (state) {
                DominantState.READY -> "Bring your watch nearby, then tap Sync."
                DominantState.WAITING -> "Preparing the next write."
                DominantState.SYNCING -> "Writing one workout with a deterministic identity."
                DominantState.SUCCESS -> "Available for GymRats to import when you open the app."
                DominantState.ERROR -> "Review the summary below, then try again."
            },
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.bodyLarge,
        )
        if (fixture == PrototypeState.COMPOSED_REPRESENTATIVE) {
            Spacer(Modifier.height(ConceptATokens.gapContent))
            Text(
                text = "Attempt ${sync.attemptCount} · phase ${sync.phase.displayName().lowercase()}",
                color = HuaweiSyncTheme.colors.ink3,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun StateDot(state: DominantState) {
    val motion = HuaweiSyncMotion.current
    val baseColor = when (state) {
        DominantState.READY -> HuaweiSyncTheme.colors.ink3
        DominantState.WAITING -> HuaweiSyncTheme.colors.info
        DominantState.SYNCING -> HuaweiSyncTheme.colors.info
        DominantState.SUCCESS -> HuaweiSyncTheme.colors.ok
        DominantState.ERROR -> HuaweiSyncTheme.colors.accent
    }
    val alpha: Float = if (motion.reducedMotion ||
        (state != DominantState.WAITING && state != DominantState.SYNCING)
    ) {
        1f
    } else {
        val transition = rememberInfiniteTransition(label = "concept-a-pulse")
        val value by transition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "concept-a-pulse-alpha",
        )
        value
    }
    Box(
        modifier = Modifier
            .size(ConceptATokens.pulseDotSize)
            .clip(CircleShape)
            .background(baseColor.copy(alpha = alpha))
            .semantics { contentDescription = state.eyebrow() },
    )
}

@Composable
private fun LedgerCard(sync: ProductSyncState) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ConceptATokens.cardShape)
            .background(HuaweiSyncTheme.colors.surface1)
            .padding(ConceptATokens.paddingCardInner)
            .animateContentSize()
            .testTag("concept-a-ledger"),
    ) {
        Text(
            text = "Your history",
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(ConceptATokens.gapContent))
        if (sync.ledgerWorkoutCount == 0) {
            Text(
                text = "No workouts recorded yet.",
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = sync.ledgerWorkoutCount.toString(),
                    color = HuaweiSyncTheme.colors.ink,
                    style = MaterialTheme.typography.displayMedium,
                )
                Spacer(Modifier.size(ConceptATokens.gapContent))
                Text(
                    text = if (sync.ledgerWorkoutCount == 1) "workout" else "workouts",
                    color = HuaweiSyncTheme.colors.ink2,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Tracked in the durable ledger.",
                color = HuaweiSyncTheme.colors.ink3,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PipelineCard(sync: ProductSyncState) {
    val presentation = PipelinePresentation.from(
        state = sync,
        coordinatorBusy = sync.phase != ProductSyncPhase.IDLE,
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ConceptATokens.cardShape)
            .background(HuaweiSyncTheme.colors.surface1)
            .padding(ConceptATokens.paddingCardInner)
            .testTag("concept-a-pipeline"),
    ) {
        Text(
            text = "Sync path",
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.titleLarge,
        )
        Spacer(Modifier.height(ConceptATokens.gapContent))
        Text(
            text = sync.healthConnectStatus.label,
            color = HuaweiSyncTheme.colors.ink2,
            style = MaterialTheme.typography.bodyMedium,
        )
        Spacer(Modifier.height(ConceptATokens.gapCards))
        SyncPipelineRail(
            model = presentation.rail,
            orientation = SyncRailOrientation.HORIZONTAL,
        )
    }
}

@Composable
private fun GymRatsCard() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ConceptATokens.insetShape)
            .background(HuaweiSyncTheme.colors.surface2)
            .padding(ConceptATokens.paddingCardInner)
            .testTag("concept-a-gymrats"),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(HuaweiSyncTheme.colors.surface3),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "GR",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Spacer(Modifier.size(ConceptATokens.gapContent))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "GymRats",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = ProductGymRatsStatus.READY_TO_READ.label,
                color = HuaweiSyncTheme.colors.ink2,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun FailureCard(summary: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ConceptATokens.cardShape)
            .background(brush = ConceptATokens.errorWash())
            .background(HuaweiSyncTheme.colors.surface1.copy(alpha = 0.6f))
            .padding(ConceptATokens.paddingCardInner)
            .testTag("concept-a-failure"),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.ErrorOutline,
                contentDescription = null,
                tint = HuaweiSyncTheme.colors.accent,
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.size(ConceptATokens.gapContent))
            Text(
                text = "Last sync",
                color = HuaweiSyncTheme.colors.ink,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Spacer(Modifier.height(ConceptATokens.gapContent))
        Text(
            text = summary,
            color = HuaweiSyncTheme.colors.ink,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

private enum class DominantState { READY, WAITING, SYNCING, SUCCESS, ERROR }

private fun DominantState.eyebrow(): String = when (this) {
    DominantState.READY -> "Ready"
    DominantState.WAITING -> "Waiting"
    DominantState.SYNCING -> "Syncing"
    DominantState.SUCCESS -> "Confirmed"
    DominantState.ERROR -> "Needs attention"
}

private fun ProductSyncState.dominantConceptAState(): DominantState = when {
    healthConnectStatus == ProductHealthConnectStatus.FAILED ||
        healthConnectStatus == ProductHealthConnectStatus.RECONCILIATION_REQUIRED ||
        healthConnectStatus == ProductHealthConnectStatus.RETRY_REQUIRED ||
        healthConnectStatus == ProductHealthConnectStatus.ACTION_REQUIRED ||
        healthConnectStatus == ProductHealthConnectStatus.UNAVAILABLE ||
        healthConnectStatus == ProductHealthConnectStatus.UPDATE_REQUIRED ||
        healthConnectStatus == ProductHealthConnectStatus.PERMISSION_REQUIRED -> DominantState.ERROR
    healthConnectStatus == ProductHealthConnectStatus.CONFIRMED_IN_HEALTH_CONNECT -> DominantState.SUCCESS
    phase == ProductSyncPhase.WRITE ||
        phase == ProductSyncPhase.ACCEPTANCE ||
        phase == ProductSyncPhase.VERIFICATION ||
        phase == ProductSyncPhase.RECONCILIATION ||
        healthConnectStatus == ProductHealthConnectStatus.WRITE_IN_PROGRESS ||
        healthConnectStatus == ProductHealthConnectStatus.ACCEPTED_AWAITING_READBACK -> DominantState.SYNCING
    phase == ProductSyncPhase.PREFLIGHT -> DominantState.WAITING
    else -> DominantState.READY
}

private fun ProductSyncPhase.displayName(): String = when (this) {
    ProductSyncPhase.IDLE -> "Idle"
    ProductSyncPhase.PREFLIGHT -> "Preflight"
    ProductSyncPhase.WRITE -> "Write"
    ProductSyncPhase.ACCEPTANCE -> "Acceptance"
    ProductSyncPhase.VERIFICATION -> "Verification"
    ProductSyncPhase.RECONCILIATION -> "Reconciliation"
}

@androidx.compose.ui.tooling.preview.Preview(name = "Concept A · dark · success", showBackground = true)
@androidx.compose.runtime.Composable
private fun ConceptASuccessDarkPreview() {
    dev.lui.huaweisync.ui.theme.HuaweiSyncTheme(darkTheme = true) {
        ConceptADashboard(
            sync = dev.lui.huaweisync.ui.prototypes.PrototypeGalleryFixtures
                .stateFor(dev.lui.huaweisync.ui.prototypes.PrototypeState.SUCCESS_CONFIRMED),
            fixture = dev.lui.huaweisync.ui.prototypes.PrototypeState.SUCCESS_CONFIRMED,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "Concept A · light · syncing", showBackground = true)
@androidx.compose.runtime.Composable
private fun ConceptASyncingLightPreview() {
    dev.lui.huaweisync.ui.theme.HuaweiSyncTheme(darkTheme = false) {
        ConceptADashboard(
            sync = dev.lui.huaweisync.ui.prototypes.PrototypeGalleryFixtures
                .stateFor(dev.lui.huaweisync.ui.prototypes.PrototypeState.SYNCING),
            fixture = dev.lui.huaweisync.ui.prototypes.PrototypeState.SYNCING,
        )
    }
}
