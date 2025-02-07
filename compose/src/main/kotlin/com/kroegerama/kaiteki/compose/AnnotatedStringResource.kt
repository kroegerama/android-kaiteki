package com.kroegerama.kaiteki.compose

import android.graphics.Typeface
import android.os.Build
import android.text.Annotation
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.AbsoluteSizeSpan
import android.text.style.AlignmentSpan
import android.text.style.BackgroundColorSpan
import android.text.style.BulletSpan
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.QuoteSpan
import android.text.style.RelativeSizeSpan
import android.text.style.ScaleXSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.TypefaceSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextGeometricTransform
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.backgroundColor
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.inSpans
import androidx.core.text.italic
import androidx.core.text.scale
import androidx.core.text.strikeThrough
import androidx.core.text.subscript
import androidx.core.text.superscript
import androidx.core.text.underline
import kotlinx.collections.immutable.PersistentMap
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

@ReadOnlyComposable
@Composable
fun defaultHtmlTextLinkStyles() = TextLinkStyles(
    style = SpanStyle(
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline
    ),
    focusedStyle = SpanStyle(
        background = MaterialTheme.colorScheme.primaryContainer
    ),
    hoveredStyle = SpanStyle(
        background = MaterialTheme.colorScheme.primaryContainer
    ),
    pressedStyle = SpanStyle(
        background = MaterialTheme.colorScheme.primaryContainer
    )
)

@ReadOnlyComposable
@Stable
@Composable
fun annotatedStringResource(
    @StringRes id: Int,
    variables: PersistentMap<String, String>,
    density: Density = LocalDensity.current,
    textLinkStyles: TextLinkStyles? = defaultHtmlTextLinkStyles()
): AnnotatedString {
    LocalConfiguration.current
    val resources = LocalContext.current.resources
    val text = resources.getText(id)
    return SpannableStringBuilder(text).replaceVariables(
        variables = variables
    ).asAnnotatedString(
        density = density,
        textLinkStyles = textLinkStyles
    )
}

private fun SpannableStringBuilder.replaceVariables(
    variables: PersistentMap<String, String>,
): SpannableStringBuilder {
    getSpans(0, length, Annotation::class.java)
        .filterIsInstance<Annotation>()
        .filter { annotation ->
            annotation.key == "var"
        }
        .forEach { annotation ->
            val replacement = variables[annotation.value] ?: return@forEach
            replace(getSpanStart(annotation), getSpanEnd(annotation), replacement)
        }
    return this
}

@ReadOnlyComposable
@Stable
@Composable
fun CharSequence.asAnnotatedString(
    density: Density = LocalDensity.current,
    textLinkStyles: TextLinkStyles? = defaultHtmlTextLinkStyles()
): AnnotatedString {
    val string = toString()
    if (this !is Spanned) return AnnotatedString(string)
    val view = LocalView.current

    val builder = AnnotatedString.Builder(string)
    getSpans(0, length, Any::class.java).forEach { span ->
        val start = getSpanStart(span)
        val end = getSpanEnd(span)

        span.spanAsSpanStyle(density)?.let {
            builder.addStyle(it, start, end)
        }
        span.spanAsParagraphStyle(density)?.let {
            builder.addStyle(it, start, end)
        }

        when (span) {
            is URLSpan -> {
                val url = span.url
                val annotation = LinkAnnotation.Url(
                    url = url,
                    styles = textLinkStyles
                )
                builder.addLink(annotation, start, end)
            }

            is ClickableSpan -> {
                val annotation = LinkAnnotation.Clickable(
                    tag = UUID.randomUUID().toString(),
                    styles = textLinkStyles,
                    linkInteractionListener = {
                        span.onClick(view)
                    }
                )
                builder.addLink(annotation, start, end)
            }

            is BulletSpan -> {
                Log.w("AnnotatedString", "BulletSpan not yet supported.")
            }
        }
    }
    return builder.toAnnotatedString()
}

private fun Any.spanAsSpanStyle(density: Density): SpanStyle? = when (this) {
    is StyleSpan -> toSpanStyle()
    is TypefaceSpan -> toSpanStyle()
    is AbsoluteSizeSpan -> with(density) { SpanStyle(fontSize = if (dip) size.dp.toSp() else size.toSp()) }
    is RelativeSizeSpan -> SpanStyle(fontSize = sizeChange.em)
    is StrikethroughSpan -> SpanStyle(textDecoration = TextDecoration.LineThrough)
    is UnderlineSpan -> SpanStyle(textDecoration = TextDecoration.Underline)
    is SuperscriptSpan -> SpanStyle(baselineShift = BaselineShift.Superscript)
    is SubscriptSpan -> SpanStyle(baselineShift = BaselineShift.Subscript)
    is ForegroundColorSpan -> SpanStyle(color = Color(foregroundColor))
    is BackgroundColorSpan -> SpanStyle(background = Color(backgroundColor))
    is ScaleXSpan -> SpanStyle(textGeometricTransform = TextGeometricTransform(scaleX = scaleX))
    else -> null
}

private fun Any.spanAsParagraphStyle(density: Density): ParagraphStyle? = when (this) {
    is QuoteSpan -> with(density) {
        ParagraphStyle(
            textIndent = TextIndent(
                getLeadingMargin(true).toSp(),
                getLeadingMargin(false).toSp()
            )
        )
    }

    is AlignmentSpan -> toParagraphStyle()

    else -> null
}

private fun StyleSpan.toSpanStyle(): SpanStyle? = when (style) {
    Typeface.NORMAL -> SpanStyle(
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal
    )

    Typeface.BOLD -> SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Normal
    )

    Typeface.ITALIC -> SpanStyle(
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic
    )

    Typeface.BOLD_ITALIC -> SpanStyle(
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Italic
    )

    else -> null
}

private fun TypefaceSpan.toSpanStyle(): SpanStyle? {
    return SpanStyle(
        fontFamily = when (family) {
            FontFamily.SansSerif.name -> FontFamily.SansSerif
            FontFamily.Serif.name -> FontFamily.Serif
            FontFamily.Monospace.name -> FontFamily.Monospace
            FontFamily.Cursive.name -> FontFamily.Cursive
            else -> return null
        }
    )
}

private fun AlignmentSpan.toParagraphStyle(): ParagraphStyle? {
    return ParagraphStyle(
        textAlign = when (this.alignment) {
            Layout.Alignment.ALIGN_NORMAL -> TextAlign.Start
            Layout.Alignment.ALIGN_CENTER -> TextAlign.Center
            Layout.Alignment.ALIGN_OPPOSITE -> TextAlign.End
            else -> return null
        }
    )
}

@Preview(showBackground = true, device = "spec:parent=pixel_tablet,orientation=portrait")
@Composable
private fun SpanStylePreview() {
    val density = LocalDensity.current
    var urlClicked by remember { mutableStateOf("") }
    var clickableClicked by remember { mutableStateOf("") }

    fun getTime() = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS).toString()

    val uriHandler = remember {
        object : UriHandler {
            override fun openUri(uri: String) {
                urlClicked = "${getTime()}> $uri"
            }
        }
    }

    val spannable = remember {
        buildSpannedString {
            bold {
                append("Hello")
            }
            append(" ")
            italic {
                append("World")
            }
            appendLine("!")

            underline {
                append("Underline")
            }

            strikeThrough {
                append("StrikeThrough")
            }
            appendLine()

            subscript {
                append("subscript")
            }
            append(" Default ")
            superscript {
                append("superscript")
            }
            appendLine()

            scale(.5f) {
                append("Scale .5f")
            }
            append(" ")
            scale(1f) {
                append("Scale 1f")
            }
            append(" ")
            scale(1.5f) {
                append("Scale 1.5f")
            }
            appendLine()

            inSpans(AbsoluteSizeSpan(20)) {
                append("AbsoluteSize 20px")
            }
            append(" ")
            inSpans(AbsoluteSizeSpan(20, true)) {
                append("AbsoluteSize 20 dip")
            }
            appendLine()

            color(Color.Cyan.toArgb()) {
                append("Cyan")
            }
            color(Color.Magenta.toArgb()) {
                append("Magenta")
            }
            color(Color.Yellow.toArgb()) {
                append("Yellow")
            }
            appendLine()

            backgroundColor(Color.LightGray.toArgb()) {
                appendLine("BackgroundColor")
            }

            inSpans(
                BackgroundColorSpan(Color.Black.toArgb()),
                ForegroundColorSpan(Color.Red.toArgb())
            ) {
                appendLine("Background with Foreground")
            }

            inSpans(ScaleXSpan(2f)) {
                appendLine("ScaleX 2f")
            }
            inSpans(QuoteSpan()) {
                appendLine("Quote1")
            }
            inSpans(QuoteSpan(Color.Green.toArgb())) {
                appendLine("Quote2")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                inSpans(QuoteSpan(
                    Color.Red.toArgb(),
                    with(density) { 4.dp.roundToPx() },
                    with(density) { 8.dp.roundToPx() }
                )) {
                    appendLine("Quote3")
                }
            }
            inSpans(URLSpan("https://google.com")) {
                append("URLSpan")
            }
            appendLine()
            inSpans(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    clickableClicked = getTime()
                }
            }) {
                append("ClickableSpan")
            }
            appendLine()
            inSpans(AlignmentSpan.Standard(Layout.Alignment.ALIGN_OPPOSITE)) {
                append("Opposite")
            }
            appendLine()
            inSpans(AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER)) {
                append("Center")
            }
        }
    }
    CompositionLocalProvider(
        LocalUriHandler provides uriHandler
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(16.dp)
        ) {
            Text(
                text = "URLSpan: $urlClicked"
            )
            Text(
                text = "ClickableSpan: $clickableClicked"
            )
            Box(modifier = Modifier.size(16.dp))
            Text(
                text = "Compose Text()",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = spannable.asAnnotatedString(),
                color = Color.Black,
                fontSize = 16.sp,
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .padding(8.dp)
            )
            Box(modifier = Modifier.size(16.dp))
            Text(
                text = "Legacy TextView()",
                style = MaterialTheme.typography.labelSmall
            )
            AndroidView(
                {
                    TextView(it).apply {
                        setTextColor(Color.Black.toArgb())
                        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                        movementMethod = LinkMovementMethod.getInstance()
                    }
                },
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .padding(8.dp)
            ) { textView ->
                textView.text = spannable
            }
        }
    }
}
