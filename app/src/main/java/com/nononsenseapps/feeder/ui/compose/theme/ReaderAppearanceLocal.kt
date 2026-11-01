package com.nononsenseapps.feeder.ui.compose.theme

import androidx.compose.runtime.compositionLocalOf
import com.nononsenseapps.feeder.archmodel.ListAppearance
import com.nononsenseapps.feeder.archmodel.ReaderAppearance

// Fork: see ReaderAppearance
val LocalReaderAppearance = compositionLocalOf { ReaderAppearance() }

// Fork: see ListAppearance
val LocalListAppearance = compositionLocalOf { ListAppearance() }

/** How much bigger the action icons are. Only the article list sets it, so the reader keeps its size. */
val LocalListIconScale = compositionLocalOf { 1f }
