package com.asfoundation.wallet.service

import android.util.Log
import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.EwtAuthenticatorService
import io.reactivex.schedulers.Schedulers
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 *
 * This Interceptor adds the EWT authentication (JWT-JsonWebToken like)
 * This authentication is only needed for the broker, inapp and deeplink Apis
 *
 */

class EwtAuthenticationInterceptor(private val walletService: WalletService,
                                   private val ewtAuthenticatorService: EwtAuthenticatorService) :
    Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()

    val ewtAuth = walletService.getWalletAddress()
        .flatMap {
          ewtAuthenticatorService.getEwtAuthentication(it, System.currentTimeMillis() / 1000L)
              .subscribeOn(Schedulers.io())
        }
        .blockingGet()

    if (ewtAuth != "Error") {
      val requestWithEwt = originalRequest.newBuilder()
          .addHeader("{Authorization", "$ewtAuth}")
          .build()
      return chain.proceed(requestWithEwt)
    }
    Log.w(EwtAuthenticationInterceptor::class.java.name, "Ewt Authentication failed")
    return chain.proceed(chain.request())
  }

}