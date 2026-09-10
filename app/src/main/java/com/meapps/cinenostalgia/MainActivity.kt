package com.meapps.cinenostalgia

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.meapps.cinenostalgia.ui.CineNostalgiaApp
import com.meapps.cinenostalgia.ui.theme.MEAppsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as CineNostalgiaApplication
        setContent {
            val fontScale by app.settingsRepository.fontScale.collectAsStateWithLifecycle(initialValue = 1f)
            MEAppsTheme(fontScaleMultiplier = fontScale) { CineNostalgiaApp() }
        }
    }
}
