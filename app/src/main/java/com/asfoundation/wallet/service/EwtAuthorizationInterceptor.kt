package com.asfoundation.wallet.service

import android.util.Log
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.EwtAuthenticatorService
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.net.ssl.HttpsURLConnection

/**
 *
 * This Interceptor adds the EWT authentication (JWT-JsonWebToken like)
 *
 */

class EwtAuthenticationInterceptor(private val walletService: WalletService,
                                   private val ewtAuthenticatorService: EwtAuthenticatorService) :
    Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val currentUnixTime = System.currentTimeMillis() / 1000L
    val address = walletService.getWalletAddress()
        .blockingGet()
    val ewtAuth = ewtAuthenticatorService.getEwtAuthentication(address, currentUnixTime)
    val response = addEwtAuthentication(ewtAuth, originalRequest, chain)

    return handleUnauthorized(response, currentUnixTime, originalRequest, chain)
  }

  private fun addEwtAuthentication(ewtAuth: String, originalRequest: Request,
                                   chain: Interceptor.Chain): Response {
    return if (ewtAuth != "Error") {
      val requestWithEwt = originalRequest.newBuilder()
          .addHeader("{Authorization", "$ewtAuth}")
          .build()
      chain.proceed(requestWithEwt)
    } else {
      Log.w(EwtAuthenticationInterceptor::class.java.name, "Ewt Authentication failed")
      chain.proceed(originalRequest)
    }
  }

  private fun handleUnauthorized(response: Response, currentUnixTime: Long,
                                 originalRequest: Request, chain: Interceptor.Chain): Response {
    return if (!response.isSuccessful && response.code() == HttpsURLConnection.HTTP_UNAUTHORIZED) {
      val address = walletService.getWalletAddress()
          .blockingGet()
      val ewtAuth = ewtAuthenticatorService.getNewEwtAuthentication(address, currentUnixTime)
      addEwtAuthentication(ewtAuth, originalRequest, chain)
    } else {
      response
    }
  }

}