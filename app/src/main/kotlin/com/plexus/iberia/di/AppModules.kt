package com.plexus.iberia.di

import com.plexus.iberia.data.di.dataModule
import com.plexus.iberia.domain.di.domainModule
import com.plexus.iberia.ui.di.uiModule

val appModules = listOf(domainModule, dataModule, uiModule)
