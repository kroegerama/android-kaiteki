package com.kroegerama.kaiteki

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.google.android.material.R
import com.google.android.material.color.MaterialColors

fun Context.openCustomChromeTab(
    uri: Uri,
    block: CustomTabsIntent.Builder.() -> Unit = {
        setDefaultColorSchemeParams(
            CustomTabColorSchemeParams.Builder()
                .setToolbarColor(
                    MaterialColors.getColor(
                        this@openCustomChromeTab,
                        R.attr.colorPrimary,
                        Color.WHITE
                    )
                )
                .setSecondaryToolbarColor(
                    MaterialColors.getColor(
                        this@openCustomChromeTab,
                        R.attr.colorPrimaryVariant,
                        Color.WHITE
                    )
                )
                .build()
        )
    }
): Boolean {
    CustomTabsIntent.Builder()
        .apply(block)
        .build().apply {
            val fixedUri = if (uri.scheme in listOf("http", "https")) {
                uri
            } else {
                uri.buildUpon().scheme("http").build()
            }
            val success = try {
                launchUrl(this@openCustomChromeTab, fixedUri)
                true
            } catch (e: Exception) {
                false
            }
            if (success) return true
            return try {
                startActivity(Intent(Intent.ACTION_VIEW).apply { data = fixedUri })
                true
            } catch (e: Exception) {
                false
            }
        }
}
