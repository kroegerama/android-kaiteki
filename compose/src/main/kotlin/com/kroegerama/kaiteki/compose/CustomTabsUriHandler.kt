package com.kroegerama.kaiteki.compose

import android.content.Context
import android.content.Intent
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri

class ChromeCustomTabsUriHandler(
    private val context: Context,
    colorSchemeParamsDecorator: CustomTabColorSchemeParams.Builder.() -> Unit = {},
    builderDecorator: CustomTabsIntent.Builder.() -> Unit = {}
) : UriHandler {

    private val customTabsIntent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .apply(colorSchemeParamsDecorator)
                .build()
        )
        .apply(builderDecorator)
        .build()

    override fun openUri(uri: String) {
        val netUri = try {
            uri.toUri()
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }
        try {
            customTabsIntent.launchUrl(context, netUri)
            return
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, netUri))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberChromeCustomTabUriHandler(
    toolbarColor: Color = MaterialTheme.colorScheme.surface,
    secondaryToolbarColor: Color = MaterialTheme.colorScheme.background
): UriHandler {
    val context = LocalContext.current
    return remember(toolbarColor, secondaryToolbarColor) {
        ChromeCustomTabsUriHandler(
            context = context,
            colorSchemeParamsDecorator = {
                setToolbarColor(toolbarColor.toArgb())
                setSecondaryToolbarColor(secondaryToolbarColor.toArgb())
            }
        )
    }
}
