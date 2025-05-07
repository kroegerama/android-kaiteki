package com.kroegerama.kaiteki.compose.composables

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.withSign
import android.graphics.Color as AndroidColor

@Composable
fun BlurHash(
    blurHash: String,
    modifier: Modifier = Modifier,
    fallback: Color = Color.Transparent
) {
    val painter = rememberBlurHashPainter(
        blurHash = blurHash,
        fallback = fallback
    )

    Box(
        modifier = modifier.paint(
            painter = painter,
            contentScale = ContentScale.FillBounds
        )
    )
}

@Composable
fun rememberBlurHashPainter(
    blurHash: String,
    fallback: Color = Color.Transparent,
    filterQuality: FilterQuality = FilterQuality.Low
): Painter {
    val localInspection = LocalInspectionMode.current
    var bitmap: Bitmap? by remember {
        mutableStateOf(
            if (localInspection) {
                runBlocking {
                    BlurHashDecoder.decode(blurHash)
                }
            } else {
                null
            }
        )
    }
    LaunchedEffect(blurHash) {
        bitmap = BlurHashDecoder.decode(blurHash)
    }
    return remember(bitmap, fallback, filterQuality) {
        bitmap?.let {
            BitmapPainter(
                image = it.asImageBitmap(),
                filterQuality = filterQuality
            )
        } ?: ColorPainter(fallback)
    }
}

@Preview(showBackground = true)
@Composable
private fun BlurHashPreview() {
    val hashes = persistentListOf(
        "W7D[kP%ZKrogT1D*=*ERACSgxsfz0L#rrwxZM|w|1Y,.|uxaxc9t",
        "VHF5?xYk^6#M9w@-5b,1J5O[@[or[k6.O[};FxngOZE3"
    )
    var idx by remember { mutableIntStateOf(0) }

    Column {
        BlurHash(
            blurHash = hashes[idx],
            modifier = Modifier
                .size(250.dp, 200.dp)
                .clickable {
                    idx = (idx + 1) % hashes.size
                }
        )
        BlurHash(
            blurHash = "invalid",
            modifier = Modifier
                .size(250.dp, 200.dp)
                .background(Color.DarkGray)
        )
        BlurHash(
            blurHash = hashes[(idx + 1) % hashes.size],
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    idx = (idx + 1) % hashes.size
                }
        )
    }
}

object BlurHashDecoder {

    class CalculableDoubleArray(
        val array: DoubleArray,
        var calculated: Boolean
    )

    private val cacheCosinesX = HashMap<Int, CalculableDoubleArray>()
    private val cacheCosinesY = HashMap<Int, CalculableDoubleArray>()

    suspend fun decode(blurHash: String?, punch: Float = 1f): Bitmap? = withContext(Dispatchers.Default) {
        decodeInternal(blurHash, punch)
    }

    private fun decodeInternal(blurHash: String?, punch: Float): Bitmap? {
        if (blurHash == null || blurHash.length < 6) {
            return null
        }
        val numCompEnc = decode83(blurHash, 0, 1)
        val numCompX = (numCompEnc % 9) + 1
        val numCompY = (numCompEnc / 9) + 1
        if (blurHash.length != 4 + 2 * numCompX * numCompY) {
            return null
        }
        val maxAcEnc = decode83(blurHash, 1, 2)
        val maxAc = (maxAcEnc + 1) / 166f
        val colors = Array(numCompX * numCompY) { i ->
            if (i == 0) {
                val colorEnc = decode83(blurHash, 2, 6)
                decodeDc(colorEnc)
            } else {
                val from = 4 + i * 2
                val colorEnc = decode83(blurHash, from, from + 2)
                decodeAc(colorEnc, maxAc * punch)
            }
        }
        return composeBitmap(numCompX, numCompY, colors)
    }

    private fun decode83(str: String, from: Int = 0, to: Int = str.length): Int {
        var result = 0
        for (i in from..<to) {
            val index = charMap[str[i]] ?: -1
            if (index != -1) {
                result = result * 83 + index
            }
        }
        return result
    }

    private fun decodeDc(colorEnc: Int): FloatArray {
        val r = colorEnc shr 16
        val g = (colorEnc shr 8) and 255
        val b = colorEnc and 255
        return floatArrayOf(srgbToLinear(r), srgbToLinear(g), srgbToLinear(b))
    }

    private fun srgbToLinear(colorEnc: Int): Float {
        val v = colorEnc / 255f
        return if (v <= 0.04045f) {
            (v / 12.92f)
        } else {
            ((v + 0.055f) / 1.055f).pow(2.4f)
        }
    }

    private fun decodeAc(value: Int, maxAc: Float): FloatArray {
        val r = value / (19 * 19)
        val g = (value / 19) % 19
        val b = value % 19
        return floatArrayOf(
            signedPow2((r - 9) / 9.0f) * maxAc,
            signedPow2((g - 9) / 9.0f) * maxAc,
            signedPow2((b - 9) / 9.0f) * maxAc
        )
    }

    private fun signedPow2(value: Float) = value.pow(2f).withSign(value)

    private fun composeBitmap(
        numCompX: Int, numCompY: Int,
        colors: Array<FloatArray>
    ): Bitmap {
        val width = numCompX * 8
        val height = numCompY * 8

        val imageArray = IntArray(width * height)
        val cosinesX = getArrayForCosinesX(width, numCompX)
        val cosinesY = getArrayForCosinesY(height, numCompY)
        for (y in 0..<height) {
            for (x in 0..<width) {
                var r = 0f
                var g = 0f
                var b = 0f
                for (j in 0..<numCompY) {
                    for (i in 0..<numCompX) {
                        val cosX = cosinesX.getCos(i, numCompX, x, width)
                        val cosY = cosinesY.getCos(j, numCompY, y, height)
                        val basis = (cosX * cosY).toFloat()
                        val color = colors[j * numCompX + i]
                        r += color[0] * basis
                        g += color[1] * basis
                        b += color[2] * basis
                    }
                }
                imageArray[x + width * y] = AndroidColor.rgb(linearToSrgb(r), linearToSrgb(g), linearToSrgb(b))
            }
        }
        cosinesX.calculated = true
        cosinesY.calculated = true
        return Bitmap.createBitmap(imageArray, width, height, Bitmap.Config.ARGB_8888)
    }

    private fun getArrayForCosinesX(width: Int, numCompX: Int): CalculableDoubleArray =
        cacheCosinesX.getOrPut(width * numCompX) {
            CalculableDoubleArray(
                array = DoubleArray(width * numCompX),
                calculated = false
            )
        }

    private fun getArrayForCosinesY(height: Int, numCompY: Int): CalculableDoubleArray =
        cacheCosinesY.getOrPut(height * numCompY) {
            CalculableDoubleArray(
                array = DoubleArray(height * numCompY),
                calculated = false
            )
        }

    private fun CalculableDoubleArray.getCos(
        x: Int,
        numComp: Int,
        y: Int,
        size: Int
    ): Double {
        val k = x + numComp * y
        if (!calculated) {
            array[k] = cos(Math.PI * y * x / size)
        }
        return array[k]
    }

    private fun linearToSrgb(value: Float): Int {
        val v = value.coerceIn(0f, 1f)
        return if (v <= 0.0031308f) {
            (v * 12.92f * 255f + 0.5f).toInt()
        } else {
            ((1.055f * v.pow(1 / 2.4f) - 0.055f) * 255 + 0.5f).toInt()
        }
    }

    private val charMap =
        "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz#\$%*+,-.:;=?@[]^_{|}~".mapIndexed { i, c ->
            c to i
        }.toMap()

}
