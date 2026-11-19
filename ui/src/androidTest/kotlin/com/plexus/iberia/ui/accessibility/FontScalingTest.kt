package com.plexus.iberia.ui.accessibility

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.DeviceConfigurationOverride
import androidx.compose.ui.test.FontScale
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertHeightIsAtLeast
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.detail.DetailScreen
import com.plexus.iberia.ui.detail.DetailState
import com.plexus.iberia.ui.detail.components.DetailInfoRow
import com.plexus.iberia.ui.home.HomeScreen
import com.plexus.iberia.ui.home.HomeState
import com.plexus.iberia.ui.home.components.ImageListItem
import com.plexus.iberia.ui.home.components.SearchBar
import com.plexus.iberia.ui.theme.IberiaTheme
import org.junit.Rule
import org.junit.Test

class FontScalingTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val longText = "Hubble captures a spectacular landscape in the Carina Nebula, ".repeat(4).trim()

    @Test
    fun longListTitlesWrapInsteadOfBeingCut() {
        setLargeFontContent {
            Box(Modifier.width(360.dp)) {
                ImageListItem(
                    image = PreviewData.image.copy(title = longText),
                    onClick = {},
                    modifier = Modifier.testTag(ITEM),
                )
            }
        }

        composeRule.onNodeWithTag(ITEM).assertHeightIsAtLeast(300.dp)
    }

    @Test
    fun longDetailValuesWrapInsideTheScreen() {
        setLargeFontContent {
            Box(Modifier.width(360.dp)) {
                DetailInfoRow(label = "Photographer", value = longText, modifier = Modifier.testTag(ROW))
            }
        }

        composeRule.onNodeWithTag(ROW).assertHeightIsAtLeast(150.dp)
    }

    @Test
    fun detailTextsAreNeverClipped() {
        val image = PreviewData.image.copy(title = longText, description = longText)
        setLargeFontContent {
            DetailScreen(state = DetailState(image = image, isLoading = false), onRetry = {}, onBackClick = {})
        }

        composeRule.onAllNodesWithText(longText).fetchSemanticsNodes().forEach { node ->
            val layouts = mutableListOf<TextLayoutResult>()
            node.config[SemanticsActions.GetTextLayoutResult].action?.invoke(layouts)
            assert(!layouts.single().hasVisualOverflow) { "Text is clipped" }
        }
    }

    @Test
    fun retryStaysReachableWhenTheMessageDoesNotFit() {
        setLargeFontContent {
            Box(Modifier.height(360.dp)) {
                HomeScreen(
                    state = HomeState(error = DomainError.Network()),
                    onQueryChange = {},
                    onLoadNextPage = {},
                    onRetry = {},
                    onImageClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Retry").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun controlsKeepAUsableTouchSize() {
        setLargeFontContent { SearchBar(query = "apollo", onQueryChange = {}) }

        val clearButton = composeRule.onNodeWithContentDescription("Clear search").fetchSemanticsNode()
        val minTouchSize = with(composeRule.density) { 48.dp.toPx() }
        assert(clearButton.touchBoundsInRoot.width >= minTouchSize) { "Touch width ${clearButton.touchBoundsInRoot.width}" }
        assert(clearButton.touchBoundsInRoot.height >= minTouchSize) { "Touch height ${clearButton.touchBoundsInRoot.height}" }
        composeRule.onNode(SemanticsMatcher.keyIsDefined(SemanticsActions.SetText)).assertHeightIsAtLeast(56.dp)
    }

    @Test
    fun searchPlaceholderIsNotCut() {
        setLargeFontContent { Box(Modifier.width(360.dp)) { SearchBar(query = "", onQueryChange = {}) } }

        composeRule.onAllNodes(hasText("Search NASA images"), useUnmergedTree = true)
            .fetchSemanticsNodes()
            .forEach { node ->
                val layouts = mutableListOf<TextLayoutResult>()
                node.config.getOrElseNullable(SemanticsActions.GetTextLayoutResult) { null }?.action?.invoke(layouts)
                layouts.forEach { assert(!it.hasVisualOverflow) { "Placeholder is clipped" } }
            }
    }

    private fun setLargeFontContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            DeviceConfigurationOverride(DeviceConfigurationOverride.FontScale(LARGEST_FONT_SCALE)) {
                IberiaTheme { content() }
            }
        }
    }

    private companion object {
        const val LARGEST_FONT_SCALE = 2f
        const val ITEM = "item"
        const val ROW = "row"
    }
}
