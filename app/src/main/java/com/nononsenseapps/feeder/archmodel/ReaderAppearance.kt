package com.nononsenseapps.feeder.archmodel

// Fork: Boost-style reader appearance, on top of the existing text scale, font and justify settings
data class ReaderAppearance(
    val titleScale: Float = 1f,
    val lineSpacing: Float = 1f,
    val edgeToEdgeImages: Boolean = true,
)
