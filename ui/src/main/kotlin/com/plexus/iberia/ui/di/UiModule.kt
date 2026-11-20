package com.plexus.iberia.ui.di

import com.plexus.iberia.ui.detail.DetailViewModel
import com.plexus.iberia.ui.home.HomeViewModel
import com.plexus.iberia.ui.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val uiModule = module {
    viewModel { SplashViewModel() }
    viewModel { HomeViewModel(searchImages = get(), getRandomImages = get()) }
    viewModel { params -> DetailViewModel(nasaId = params.get(), getImage = get()) }
}
