package com.kroegerama.kaiteki

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.Fragment
import com.google.android.material.color.MaterialColors

private fun CustomTabsIntent.Builder.defaultScheme(context: Context) {
    setDefaultColorSchemeParams(
        CustomTabColorSchemeParams.Builder()
            .setToolbarColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorPrimary,
                    Color.WHITE
                )
            )
            .setSecondaryToolbarColor(
                MaterialColors.getColor(
                    context,
                    com.google.android.material.R.attr.colorPrimaryVariant,
                    Color.WHITE
                )
            )
            .build()
    )
}

fun Context.openCustomChromeTab(
    uri: Uri,
    block: CustomTabsIntent.Builder.() -> Unit = {
        defaultScheme(this@openCustomChromeTab)
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

fun Fragment.openCustomChromeTab(
    uri: Uri,
    block: CustomTabsIntent.Builder.() -> Unit = {
        defaultScheme(requireContext())
    }
) = requireContext().openCustomChromeTab(uri, block)
