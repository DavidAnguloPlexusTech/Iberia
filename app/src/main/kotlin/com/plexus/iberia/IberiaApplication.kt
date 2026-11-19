package com.plexus.iberia

import android.app.Application
import com.plexus.iberia.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class IberiaApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger()
            androidContext(this@IberiaApplication)
            modules(appModules)
        }
    }
}
