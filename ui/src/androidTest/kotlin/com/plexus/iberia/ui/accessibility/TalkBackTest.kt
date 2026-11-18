package com.plexus.iberia.ui.accessibility

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isHeading
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.plexus.iberia.domain.error.DomainError
import com.plexus.iberia.ui.common.PreviewData
import com.plexus.iberia.ui.common.components.EmptyMessage
import com.plexus.iberia.ui.common.components.ErrorMessage
import com.plexus.iberia.ui.common.components.LoadingAnimation
import com.plexus.iberia.ui.detail.DetailScreen
import com.plexus.iberia.ui.detail.DetailState
import com.plexus.iberia.ui.home.HomeScreen
import com.plexus.iberia.ui.home.HomeState
import com.plexus.iberia.ui.home.components.ImageListItem
import com.plexus.iberia.ui.home.components.SearchBar
import com.plexus.iberia.ui.theme.IberiaTheme
import org.junit.Rule
import org.junit.Test

/** What TalkBack reads: one node per item, headings, grouped info, live regions and screen titles. */
class TalkBackTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val image = PreviewData.image
    private val itemDescription = "Learjet 25 Research Aircraft. Date July 29, 2015. Center GRC"

    @Test
    fun listItemIsReadAsOneButtonWithItsDateInWords() {
        composeRule.setContent { IberiaTheme { ImageListItem(image = image, onClick = {}) } }

        composeRule.onNodeWithContentDescription(itemDescription)
            .assertHasClickAction()
            .assert(hasRole(Role.Button))
            .assert(hasClickLabel("Open details"))
        composeRule.onNodeWithText(image.title).assertDoesNotExist()
    }

    @Test
    fun searchFieldIsNamedAndItsIconIsDecorative() {
        composeRule.setContent { IberiaTheme { SearchBar(query = "apollo", onQueryChange = {}) } }

        composeRule.onNode(hasSetTextAction() and hasContentDescription("Search NASA images")).assertExists()
        composeRule.onNodeWithContentDescription("Search").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Clear search").assertHasClickAction()
    }

    @Test
    fun detailTitleIsAHeadingAndInfoIsReadAsSentences() {
        composeRule.setContent { IberiaTheme { DetailScreen(state = detailState(), onRetry = {}, onBackClick = {}) } }

        composeRule.onNode(hasText(image.title) and isHeading()).assertExists()
        composeRule.onNodeWithContentDescription("Date: July 29, 2015").assertExists()
        composeRule.onNodeWithContentDescription("Center: GRC").assertExists()
        composeRule.onNodeWithContentDescription("Photographer: ${image.photographer}").assertExists()
    }

    @Test
    fun detailPhotoIsNotReadTwice() {
        composeRule.setContent { IberiaTheme { DetailScreen(state = detailState(), onRetry = {}, onBackClick = {}) } }

        composeRule.onNodeWithContentDescription(image.title).assertDoesNotExist()
    }

    @Test
    fun screensAnnounceTheirTitle() {
        composeRule.setContent {
            IberiaTheme {
                HomeScreen(state = HomeState(), onQueryChange = {}, onLoadNextPage = {}, onRetry = {}, onImageClick = {})
            }
        }

        composeRule.onNode(hasPaneTitle("Search NASA images")).assertExists()
    }

    @Test
    fun detailScreenAnnouncesItsTitle() {
        composeRule.setContent { IberiaTheme { DetailScreen(state = detailState(), onRetry = {}, onBackClick = {}) } }

        composeRule.onNode(hasPaneTitle("Image details")).assertExists()
    }

    @Test
    fun backButtonIsReadBeforeTheContent() {
        composeRule.setContent { IberiaTheme { DetailScreen(state = detailState(), onRetry = {}, onBackClick = {}) } }

        composeRule.onNode(isReadFirst()).assertExists()
            .assert(hasAnyDescendant(hasContentDescription("Back")))
    }

    @Test
    fun searchFieldIsReadBeforeTheResults() {
        composeRule.setContent {
            IberiaTheme {
                HomeScreen(
                    state = HomeState(images = PreviewData.images),
                    onQueryChange = {},
                    onLoadNextPage = {},
                    onRetry = {},
                    onImageClick = {},
                )
            }
        }

        composeRule.onNode(isReadFirst()).assert(hasSetTextAction())
    }

    @Test
    fun loadingIsAnnounced() {
        composeRule.setContent { IberiaTheme { LoadingAnimation() } }

        composeRule.onNodeWithContentDescription("Loading").assert(hasPoliteLiveRegion())
    }

    @Test
    fun errorsAreAnnounced() {
        composeRule.setContent { IberiaTheme { ErrorMessage(message = "Check your connection.", onRetry = {}) } }

        composeRule.onNodeWithText("Check your connection.").assert(hasPoliteLiveRegion())
        composeRule.onNodeWithText("Retry").assertHasClickAction()
    }

    @Test
    fun emptyMessageTitleIsAHeadingAndItsMessageIsAnnounced() {
        composeRule.setContent { IberiaTheme { EmptyMessage(title = "Explore space", message = "Search something.") } }

        composeRule.onNode(hasText("Explore space") and isHeading()).assertExists()
        composeRule.onNodeWithText("Search something.").assert(hasPoliteLiveRegion())
    }

    @Test
    fun errorStateOfTheDetailIsAnnounced() {
        composeRule.setContent {
            IberiaTheme {
                DetailScreen(
                    state = DetailState(isLoading = false, error = DomainError.Network),
                    onRetry = {},
                    onBackClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Check your connection and try again.").assert(hasPoliteLiveRegion())
    }

    private fun detailState() = DetailState(image = image, isLoading = false)
}
