package com.meapps.cinenostalgia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.meapps.cinenostalgia.ui.CineNostalgiaApp
import com.meapps.cinenostalgia.ui.theme.MEAppsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MEAppsTheme { CineNostalgiaApp() } }
    }
}
