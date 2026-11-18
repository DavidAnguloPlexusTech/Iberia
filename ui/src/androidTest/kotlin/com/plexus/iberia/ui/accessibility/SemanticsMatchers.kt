package com.plexus.iberia.ui.accessibility

import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher

internal fun hasRole(role: Role) = SemanticsMatcher.expectValue(SemanticsProperties.Role, role)

internal fun hasPoliteLiveRegion() = SemanticsMatcher.expectValue(SemanticsProperties.LiveRegion, LiveRegionMode.Polite)

internal fun hasPaneTitle(title: String) = SemanticsMatcher.expectValue(SemanticsProperties.PaneTitle, title)

internal fun isReadFirst() = SemanticsMatcher.expectValue(SemanticsProperties.TraversalIndex, -1f)

internal fun hasClickLabel(label: String) = SemanticsMatcher("has click label \"$label\"") {
    it.config.getOrElseNullable(SemanticsActions.OnClick) { null }?.label == label
}
