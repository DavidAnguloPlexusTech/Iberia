package com.plexus.iberia.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.plexus.iberia.ui.R

/** Space Grotesk is a variable font: every weight comes from the same file. */
private val SpaceGrotesk = FontFamily(
    spaceGroteskFont(FontWeight.Normal),
    spaceGroteskFont(FontWeight.Medium),
    spaceGroteskFont(FontWeight.SemiBold),
    spaceGroteskFont(FontWeight.Bold),
)

private fun spaceGroteskFont(weight: FontWeight) = Font(
    resId = R.font.space_grotesk,
    weight = weight,
    variationSettings = FontVariation.Settings(FontVariation.weight(weight.weight)),
)

private val baseline = Typography()

/** Material type scale with Space Grotesk; titles get bold weights and wide tracking. */
internal val IberiaTypography = Typography(
    displaySmall = baseline.displaySmall.spaceGrotesk(FontWeight.Bold, letterSpacing = 1.5),
    headlineMedium = baseline.headlineMedium.spaceGrotesk(FontWeight.Bold, letterSpacing = 1.0),
    headlineSmall = baseline.headlineSmall.spaceGrotesk(FontWeight.Bold),
    titleLarge = baseline.titleLarge.spaceGrotesk(FontWeight.SemiBold),
    titleMedium = baseline.titleMedium.spaceGrotesk(FontWeight.SemiBold),
    bodyLarge = baseline.bodyLarge.spaceGrotesk(FontWeight.Normal),
    bodyMedium = baseline.bodyMedium.spaceGrotesk(FontWeight.Normal),
    bodySmall = baseline.bodySmall.spaceGrotesk(FontWeight.Normal),
    labelLarge = baseline.labelLarge.spaceGrotesk(FontWeight.Bold, letterSpacing = 1.0),
    labelMedium = baseline.labelMedium.spaceGrotesk(FontWeight.Medium, letterSpacing = 1.0),
)

private fun TextStyle.spaceGrotesk(weight: FontWeight, letterSpacing: Double? = null): TextStyle = copy(
    fontFamily = SpaceGrotesk,
    fontWeight = weight,
    letterSpacing = letterSpacing?.sp ?: this.letterSpacing,
)
