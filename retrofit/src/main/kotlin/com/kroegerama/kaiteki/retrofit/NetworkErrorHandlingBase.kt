package com.kroegerama.kaiteki.retrofit

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.fragment.app.Fragment
import arrow.core.Either
import com.kroegerama.kaiteki.architecture.popBackStack
import com.kroegerama.kaiteki.retrofit.arrow.CallError
import com.kroegerama.kaiteki.retrofit.arrow.CallErrorException
import com.kroegerama.kaiteki.retrofit.arrow.HttpError
import com.kroegerama.kaiteki.retrofit.arrow.IOError
import com.kroegerama.kaiteki.retrofit.arrow.TypedCallError
import com.kroegerama.kaiteki.retrofit.arrow.TypedHttpError
import com.kroegerama.kaiteki.retrofit.arrow.UnexpectedError
import com.kroegerama.kaiteki.showMaterialAlertDialog

abstract class NetworkErrorHandlingBase<ErrorResponse : Any, SpecificError>(
    @StringRes
    private val networkErrorTitleRes: Int,
    @StringRes
    private val networkErrorHttpRes: Int,
    @StringRes
    private val networkErrorIoRes: Int,
    @StringRes
    private val networkErrorUnexpectedRes: Int,
    @StringRes
    private val networkErrorDefaultRes: Int,
    @StyleRes
    private val alertDialogTheme: Int = 0,
) {

    protected abstract fun MutableMap<SpecificError, Int>.initSpecificErrors()
    abstract fun ErrorResponse.getSpecific(): SpecificError?

    @StringRes
    open fun Throwable.getErrorNameRes(): Int? = null

    private val specificErrorRegistry by lazy {
        mutableMapOf<SpecificError, Int>().apply {
            initSpecificErrors()
        }
    }

    private val SpecificError.nameRes get() = specificErrorRegistry[this] ?: networkErrorDefaultRes

    private val Throwable.fallbackDescription: String get() = localizedMessage ?: message ?: javaClass.simpleName

    fun Context.getNetworkErrorMessage(networkError: TypedCallError<ErrorResponse>): String =
        when (networkError) {
            is TypedHttpError -> networkError.body.getSpecific()?.nameRes?.let(::getString) ?: getString(networkErrorHttpRes, networkError.code)
            is CallError -> getNetworkErrorMessage(networkError)
        }

    fun Context.getNetworkErrorMessage(networkError: CallError): String =
        when (networkError) {
            is HttpError -> getString(networkErrorHttpRes, networkError.code)
            is IOError -> networkError.cause.getErrorNameRes()?.let(::getString) ?: getString(
                networkErrorIoRes,
                networkError.cause.fallbackDescription
            )

            is UnexpectedError -> networkError.cause.getErrorNameRes()?.let(::getString) ?: getString(
                networkErrorUnexpectedRes,
                networkError.cause.fallbackDescription
            )
        }

    fun Context.getNetworkErrorMessage(throwable: Throwable): String = when (throwable) {
        is CallErrorException -> getNetworkErrorMessage(throwable.delegate)
        else -> throwable.getErrorNameRes()?.let(::getString) ?: getString(networkErrorUnexpectedRes, throwable.fallbackDescription)
    }

    fun Context.handleError(
        networkError: TypedCallError<ErrorResponse>,
        dismissListener: () -> Unit = { }
    ) {
        val message = getNetworkErrorMessage(networkError)
        showMaterialAlertDialog(theme = alertDialogTheme) {
            setTitle(networkErrorTitleRes)
            setMessage(message)
            setPositiveButton(android.R.string.ok, null)
            setOnDismissListener { dismissListener() }
        }
    }

    fun Context.handleError(
        resource: RetrofitResource<*>,
        dismissListener: () -> Unit = { }
    ) {
        val error = when (resource) {
            is RetrofitResource.Error -> UnexpectedError(cause = resource.throwable)
            is RetrofitResource.NoSuccess -> HttpError(code = resource.code, message = "", body = resource.errorBody)
            is RetrofitResource.Running -> return
            is RetrofitResource.Success -> return
        }
        handleError(error, dismissListener)
    }

    fun Fragment.handleError(networkError: TypedCallError<ErrorResponse>) =
        requireContext().handleError(networkError)

    fun Fragment.handleErrorAndDismiss(networkError: TypedCallError<ErrorResponse>) =
        requireContext().handleError(networkError) { popBackStack() }

    fun Fragment.handleError(resource: RetrofitResource<*>) =
        requireContext().handleError(resource)

    fun Fragment.handleErrorAndDismiss(resource: RetrofitResource<*>) =
        requireContext().handleError(resource) { popBackStack() }

    val TypedCallError<ErrorResponse>.specific
        get() = when (this) {
            is TypedHttpError -> body.getSpecific()
            else -> null
        }

    inline fun <reified T> Fragment.handleResponse(
        result: Either<TypedCallError<ErrorResponse>, T>,
        onRight: (T) -> Unit = { }
    ) = result.onLeft { handleError(it) }.onRight(onRight)

    inline fun <reified T> Fragment.handleResponse(
        result: RetrofitResource<T>,
        onSuccess: (T?) -> Unit = { }
    ) = result.noSuccessOrError { handleError(this) }.success { data.let(onSuccess) }

}
