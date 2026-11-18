package com.plexus.iberia.ui.common

import com.plexus.iberia.domain.model.NasaImage

/** Sample data shared by the composable previews. */
internal object PreviewData {

    val image = NasaImage(
        id = "GRC-2015-C-04751",
        title = "Learjet 25 Research Aircraft",
        description = "NASA Glenn Mechanic checks the nose wheel axle nut on NASA Glenn's Learjet 25 research aircraft.",
        thumbnailUrl = null,
        imageUrl = null,
        dateCreated = "2015-07-29T00:00:00Z",
        center = "GRC",
        keywords = listOf("learjet", "aircraft", "research"),
        photographer = "Bridget Caswell",
    )

    val images = List(5) { index -> image.copy(id = "id-$index", title = "${image.title} $index") }
}
