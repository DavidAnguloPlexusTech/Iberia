package com.plexus.iberia.ui.accessibility

import androidx.compose.runtime.Composable
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.input.InputModeManager
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.pressKey
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.ErrorMessage
import com.plexus.iberia.ui.detail.DetailScreen
import com.plexus.iberia.ui.detail.DetailState
import com.plexus.iberia.ui.home.HomeScreen
import com.plexus.iberia.ui.home.HomeState
import com.plexus.iberia.ui.home.components.ImageList
import com.plexus.iberia.ui.home.components.ImageListItem
import com.plexus.iberia.ui.home.components.SearchBar
import com.plexus.iberia.ui.theme.IberiaTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SwitchAccessTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var inputModeManager: InputModeManager

    @Test
    fun everyActionableElementOfHomeHasAName() {
        setSwitchContent { IberiaTheme { HomeScreenWith(HomeState(query = "apollo", images = PreviewData.images)) } }

        val actionable = composeRule.onAllNodes(hasClickAction()).fetchSemanticsNodes()
        assertTrue(actionable.isNotEmpty())
        actionable.forEach { node ->
            val name = node.config.getOrElseNullable(SemanticsProperties.ContentDescription) { null }.orEmpty() +
                node.config.getOrElseNullable(SemanticsProperties.Text) { null }.orEmpty()
            assertTrue("Unnamed actionable element: $node", name.isNotEmpty())
        }
    }

    @Test
    fun detailContentIsNotAnExtraStop() {
        setSwitchContent {
            IberiaTheme {
                DetailScreen(state = DetailState(image = PreviewData.image, isLoading = false), onRetry = {}, onBackClick = {})
            }
        }

        composeRule.onAllNodes(hasClickAction()).assertCountEquals(1)
        composeRule.onNodeWithContentDescription("Back").assertExists()
    }

    @Test
    fun listItemOpensWithTheSelectKey() {
        var opened = false
        setSwitchContent { IberiaTheme { ImageListItem(image = PreviewData.image, onClick = { opened = true }) } }

        composeRule.onNode(hasClickAction()).selectWithKeyboard()

        assertTrue(opened)
    }

    @Test
    fun retryWorksWithTheSelectKey() {
        var retried = false
        setSwitchContent { IberiaTheme { ErrorMessage(message = "Error", onRetry = { retried = true }) } }

        composeRule.onNodeWithText("Retry").selectWithKeyboard()

        assertTrue(retried)
    }

    @Test
    fun clearSearchWorksWithTheSelectKey() {
        var query = "apollo"
        setSwitchContent { IberiaTheme { SearchBar(query = query, onQueryChange = { query = it }) } }

        composeRule.onNodeWithContentDescription("Clear search").selectWithKeyboard()

        assertEquals("", query)
    }

    @Test
    fun backWorksWithTheSelectKey() {
        var wentBack = false
        setSwitchContent {
            IberiaTheme {
                DetailScreen(
                    state = DetailState(image = PreviewData.image, isLoading = false),
                    onRetry = {},
                    onBackClick = { wentBack = true },
                )
            }
        }

        composeRule.onNodeWithContentDescription("Back").selectWithKeyboard()

        assertTrue(wentBack)
    }

    @Test
    fun scrollingTheListWithoutGesturesLoadsTheNextPage() {
        var nextPageRequests = 0
        val images = List(30) { PreviewData.image.copy(id = "id-$it") }
        setSwitchContent {
            IberiaTheme {
                ImageList(
                    images = images,
                    isLoadingNextPage = false,
                    error = null,
                    onImageClick = {},
                    onLoadNextPage = { nextPageRequests++ },
                    onRetry = {},
                )
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToIndex(images.lastIndex)
        composeRule.waitForIdle()

        assertTrue(nextPageRequests > 0)
    }

    @Test
    fun errorInsideTheListCanBeRetriedWithoutTouch() {
        var retried = false
        setSwitchContent {
            IberiaTheme {
                ImageList(
                    images = PreviewData.images,
                    isLoadingNextPage = false,
                    error = DomainError.Network(),
                    onImageClick = {},
                    onLoadNextPage = {},
                    onRetry = { retried = true },
                )
            }
        }

        composeRule.onNode(hasScrollAction()).performScrollToIndex(PreviewData.images.size)
        composeRule.onNodeWithText("Retry").selectWithKeyboard()

        assertTrue(retried)
    }

    @Test
    fun eachResultIsASingleStop() {
        setSwitchContent { IberiaTheme { HomeScreenWith(HomeState(query = "apollo", images = PreviewData.images)) } }

        val results = composeRule.onAllNodesWithContentDescription("Learjet", substring = true).fetchSemanticsNodes()
        assertTrue(results.isNotEmpty())
        results.forEach { node -> assertTrue(node.config.contains(SemanticsActions.OnClick)) }
    }

    private fun setSwitchContent(content: @Composable () -> Unit) {
        composeRule.setContent {
            inputModeManager = LocalInputModeManager.current
            content()
        }
    }

    private fun SemanticsNodeInteraction.selectWithKeyboard() {
        composeRule.runOnIdle { inputModeManager.requestInputMode(InputMode.Keyboard) }
        performSemanticsAction(SemanticsActions.RequestFocus)
        performKeyInput { pressKey(Key.Enter) }
    }

    @Composable
    private fun HomeScreenWith(state: HomeState) {
        HomeScreen(state = state, onQueryChange = {}, onLoadNextPage = {}, onRetry = {}, onImageClick = {})
    }
}
