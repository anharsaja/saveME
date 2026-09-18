package com.saveme.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saveme.app.data.prefs.AppSettings
import com.saveme.app.data.prefs.resolveDark
import com.saveme.app.ui.nav.SaveMeNavHost
import com.saveme.app.ui.theme.SaveMeTheme
import com.saveme.app.util.Haptics
import com.saveme.app.util.LocalHaptics

/**
 * Satu-satunya layar penuh aplikasi. Tautan yang dibagikan dari aplikasi lain
 * ditangani [ShareTargetActivity], bukan di sini.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val app = applicationContext as SaveMeApp
            val settings by app.settings.settings
                .collectAsStateWithLifecycle(initialValue = AppSettings())
            val dark = settings.themeMode.resolveDark()

            // Ikon bilah sistem harus berlawanan dengan latar aplikasi, bukan
            // mengikuti tema ponsel — kalau tidak, ikonnya bisa hilang tertelan.
            LaunchedEffect(dark) {
                val style = if (dark) {
                    SystemBarStyle.dark(Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
                }
                enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
            }

            SaveMeTheme(darkTheme = dark) {
                val feedback = LocalHapticFeedback.current
                CompositionLocalProvider(
                    LocalHaptics provides Haptics(feedback, settings.hapticsEnabled),
                ) {
                    SaveMeNavHost()
                }
            }
        }
    }
}
