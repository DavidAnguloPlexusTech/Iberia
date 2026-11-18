package com.plexus.iberia.ui.di

import com.plexus.iberia.ui.detail.DetailViewModel
import com.plexus.iberia.ui.home.HomeViewModel
import com.plexus.iberia.ui.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

/** ViewModels of every screen. */
val uiModule = module {
    viewModelOf(::SplashViewModel)
    viewModelOf(::HomeViewModel)
    viewModel { params -> DetailViewModel(nasaId = params.get(), getImage = get()) }
}
