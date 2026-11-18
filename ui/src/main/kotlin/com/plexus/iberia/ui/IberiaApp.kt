package com.plexus.iberia.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.plexus.iberia.ui.navigation.IberiaNavHost
import com.plexus.iberia.ui.theme.IberiaTheme

@Composable
fun IberiaApp() {
    IberiaTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            IberiaNavHost()
        }
    }
}
