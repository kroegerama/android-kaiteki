package com.kroegerama.kaiteki.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract

/**
 * It is recommended to add the following code to the AndroidManifest.xml:
 * ```xml
 *     <queries>
 *         <intent>
 *             <action android:name="android.provider.action.PICK_IMAGES" />
 *             <data android:mimeType="*&#47;*" />
 *         </intent>
 *         <intent>
 *             <action android:name="android.intent.action.OPEN_DOCUMENT" />
 *             <data android:mimeType="*&#47;*" />
 *         </intent>
 *     </queries>
 * ```
 */
object PickImageResultContract : ActivityResultContract<Unit, Uri?>() {

    override fun createIntent(context: Context, input: Unit): Intent {
        val pickImagesIntent = Intent("android.provider.action.PICK_IMAGES").apply {
            type = "image/*"
        }
        if (pickImagesIntent.resolveActivity(context.packageManager) != null) {
            return pickImagesIntent
        }
        return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*"))
            type = "*/*"
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.data
}
