package com.kroegerama.kaiteki

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun <T : DialogFragment> AppCompatActivity.showDialog(provider: () -> T) =
    provider().show(supportFragmentManager, null)

fun <T : DialogFragment> Fragment.showDialog(provider: () -> T) =
    provider().show(childFragmentManager, null)

fun Context.materialAlertDialog(
    @StyleRes theme: Int = 0,
    block: MaterialAlertDialogBuilder.() -> Unit
) = MaterialAlertDialogBuilder(this, theme).apply(block)

fun Fragment.materialAlertDialog(
    @StyleRes theme: Int = 0,
    block: MaterialAlertDialogBuilder.() -> Unit
) = requireContext().materialAlertDialog(theme, block)

fun Context.showMaterialAlertDialog(
    @StyleRes theme: Int = 0,
    block: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog = materialAlertDialog(theme, block).show()

fun Fragment.showMaterialAlertDialog(
    @StyleRes theme: Int = 0,
    block: MaterialAlertDialogBuilder.() -> Unit
): AlertDialog = requireContext().showMaterialAlertDialog(theme, block)
