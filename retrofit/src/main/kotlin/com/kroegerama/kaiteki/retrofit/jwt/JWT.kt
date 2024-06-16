package com.kroegerama.kaiteki.retrofit.jwt

import android.util.Base64
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import java.nio.charset.Charset
import java.util.Date

class JWT(
    val token: String
) {

    val header: Map<String, String>
    val payload: JWTPayload
    val signature: String

    init {
        val parts = token.split('.')
        if (parts.size != 3) {
            throw IllegalArgumentException("Malformed token. Needs 3 parts, but has ${parts.size} parts.")
        }
        header = mapAdapter.fromJson(parts[0].decodeBase64())!!
        payload = payloadAdapter.fromJson(parts[1].decodeBase64())!!
        signature = parts[2]
    }

    val issuer get() = payload.iss
    val subject get() = payload.sub
    val audience get() = payload.aud
    val expiresAt get() = payload.expDate
    val notBefore get() = payload.nbfDate
    val issuedAt get() = payload.iatDate
    val id get() = payload.jti
    val claims get() = payload.claims.orEmpty()

    val isExpired: Boolean
        get() {
            val now = Date().time / 1000
            val expValid = payload.exp.let { exp ->
                exp == null || now <= exp - EXP_SAFETY_SEC
            }
            val iatValid = payload.iat.let { iat ->
                iat == null || now >= iat - IAT_LENIENCE_SEC
            }
            return !expValid || !iatValid
        }

    override fun toString() = token

    companion object {
        const val EXP_SAFETY_SEC = 60
        const val IAT_LENIENCE_SEC = 60

        private val moshi by lazy {
            Moshi.Builder().build()
        }
        private val mapAdapter by lazy {
            moshi.adapter<Map<String, String>>(Types.newParameterizedType(Map::class.java, String::class.java, String::class.java))
        }
        private val payloadAdapter by lazy {
            JWTPayloadAdapter(moshi)
        }

        private fun String.decodeBase64(): String {
            val bytes = Base64.decode(this, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            return bytes.toString(Charset.defaultCharset())
        }
    }
}

class JWTPayloadAdapter(moshi: Moshi) : JsonAdapter<JWTPayload>() {
    private val options: JsonReader.Options = JsonReader.Options.of(
        "iss", "sub", "exp", "nbf", "iat", "jti", "aud"
    )
    private val nullableStringAdapter: JsonAdapter<String?> = moshi.adapter(String::class.java)
    private val stringAdapter: JsonAdapter<String?> = nullableStringAdapter.nonNull()
    private val nullableLongAdapter: JsonAdapter<Long?> = moshi.adapter(Long::class.javaObjectType)
    private val nullableStringListAdapter: JsonAdapter<List<String>?> = moshi.adapter(
        Types.newParameterizedType(List::class.java, String::class.java)
    )
    private val stringListAdapter = nullableStringListAdapter.nonNull()

    override fun fromJson(reader: JsonReader): JWTPayload? {
        var iss: String? = null
        var sub: String? = null
        var exp: Long? = null
        var nbf: Long? = null
        var iat: Long? = null
        var jti: String? = null
        var aud: List<String>? = null
        val claims = mutableMapOf<String, List<String>>()

        with(reader) {
            beginObject()

            while (hasNext()) {
                when (selectName(options)) {
                    0 -> iss = nullableStringAdapter.fromJson(this)
                    1 -> sub = nullableStringAdapter.fromJson(this)
                    2 -> exp = nullableLongAdapter.fromJson(this)
                    3 -> nbf = nullableLongAdapter.fromJson(this)
                    4 -> iat = nullableLongAdapter.fromJson(this)
                    5 -> jti = nullableStringAdapter.fromJson(this)
                    6 -> aud = when (peek()) {
                        JsonReader.Token.NULL -> null
                        JsonReader.Token.STRING -> listOf(stringAdapter.fromJson(this)!!)
                        JsonReader.Token.BEGIN_ARRAY -> nullableStringListAdapter.fromJson(this)
                        else -> throw JsonDataException()
                    }
                    else -> {
                        val name = nextName()
                        when (peek()) {
                            JsonReader.Token.STRING -> claims[name] = listOf(nextString())
                            JsonReader.Token.BEGIN_ARRAY -> claims[name] = stringListAdapter.fromJson(this)!!
                            else -> throw JsonDataException()
                        }
                    }
                }
            }
            endObject()
        }
        val nullableClaims = if (claims.isEmpty()) null else claims
        return JWTPayload(iss, sub, exp, nbf, iat, jti, aud, nullableClaims)
    }

    override fun toJson(writer: JsonWriter, value: JWTPayload?) {
        if (value == null) {
            throw NullPointerException("value was null! Wrap in .nullSafe() to write nullable values.")
        }
        with(value) {
            writer.withObject {
                named("iss") {
                    nullableStringAdapter.toJson(this, iss)
                }
                named("sub") {
                    nullableStringAdapter.toJson(this, sub)
                }
                named("exp") {
                    nullableLongAdapter.toJson(this, exp)
                }
                named("nbf") {
                    nullableLongAdapter.toJson(this, nbf)
                }
                named("iat") {
                    nullableLongAdapter.toJson(this, iat)
                }
                named("aud") {
                    if (aud?.size == 1) {
                        stringAdapter.toJson(this, aud[0])
                    } else {
                        nullableStringListAdapter.toJson(this, aud)
                    }
                }
                claims?.forEach { (k, v) ->
                    named(k) {
                        if (v.size == 1) {
                            stringAdapter.toJson(this, v[0])
                        } else {
                            stringListAdapter.toJson(this, v)
                        }
                    }
                }
            }
        }
    }

    private fun JsonWriter.withObject(block: JsonWriter.() -> Unit) {
        beginObject()
        block()
        endObject()
    }

    private fun JsonWriter.named(name: String, block: JsonWriter.() -> Unit) {
        name(name)
        block()
    }
}

data class JWTPayload(
    val iss: String? = null,
    val sub: String? = null,
    val exp: Long? = null,
    val nbf: Long? = null,
    val iat: Long? = null,
    val jti: String? = null,
    val aud: List<String>? = null,
    val claims: Map<String, List<String>>? = null
) {
    val expDate get() = exp.asDate()
    val nbfDate get() = nbf.asDate()
    val iatDate get() = iat.asDate()

    fun getClaim(key: String) = claims?.get(key)
}

internal fun Long?.asDate() = this?.let { Date(it * 1000) }
