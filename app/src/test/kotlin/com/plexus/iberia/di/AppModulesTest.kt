package com.plexus.iberia.di

import org.junit.jupiter.api.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class AppModulesTest {

    private val extraTypes = listOf(String::class)

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `all dependencies can be resolved`() {
        module { includes(appModules) }.verify(extraTypes = extraTypes)
    }
}
