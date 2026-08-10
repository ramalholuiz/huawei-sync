package dev.lui.huaweisync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import dev.lui.huaweisync.ui.components.FilterChip
import dev.lui.huaweisync.ui.components.PrimaryAction
import dev.lui.huaweisync.ui.components.SecondaryAction
import dev.lui.huaweisync.ui.components.StatusChip
import dev.lui.huaweisync.ui.components.StraightEdgeButton
import dev.lui.huaweisync.ui.components.VisualStatus
import dev.lui.huaweisync.ui.theme.HuaweiSyncTheme
import java.io.File
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class VisualComponentsContractTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun `actions and filter preserve mixed case and accessible targets`() {
        compose.setContent {
            HuaweiSyncTheme(darkTheme = false) {
                Column {
                    PrimaryAction("Sync now", onClick = {})
                    SecondaryAction("Try again", onClick = {})
                    FilterChip("Attention only", selected = true, onClick = {})
                    StraightEdgeButton("Legacy alias", onClick = {}, accent = true)
                }
            }
        }

        listOf("Sync now", "Try again", "Attention only", "Legacy alias").forEach { label ->
            compose.onNodeWithText(label)
                .assertIsDisplayed()
                .assertHasClickAction()
                .assertHeightIsAtLeast(48.dp)
        }
        compose.onNodeWithText("Attention only").assertIsSelected()
    }

    @Test
    fun `status chip exposes a readable semantic state`() {
        compose.setContent {
            HuaweiSyncTheme {
                StatusChip(label = "Attention required", status = VisualStatus.Attention)
            }
        }
        compose.onNodeWithText("Attention required").assertIsDisplayed()
    }

    @Test
    fun `raised card stays tonal and borderless by construction`() {
        val source = File("src/main/java/dev/lui/huaweisync/ui/components/HuaweiSyncComponents.kt").readText()
        val raisedCard = source.substringAfter("fun RaisedCard(").substringBefore("fun AttentionCard(")
        assertTrue(raisedCard.contains("background(HuaweiSyncTheme.colors.surface2)"))
        assertFalse(raisedCard.contains(".border("))
    }
}
