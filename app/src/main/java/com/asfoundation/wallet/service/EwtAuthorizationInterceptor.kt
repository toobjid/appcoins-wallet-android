package com.asfoundation.wallet.service

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.util.convertToBase64
import com.google.gson.JsonObject
import io.reactivex.Single
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
                                   private val header: String) : Interceptor {

  @Throws(IOException::class)
  override fun intercept(chain: Interceptor.Chain): Response {
    val originalRequest = chain.request()
    val ewtAuth = getEwtAuthentication().subscribeOn(Schedulers.io())
        .blockingGet()
    if (ewtAuth != "Error") {
      val requestWithEwt = originalRequest.newBuilder()
          .addHeader("{Authorization", ewtAuth + "}")
          .build()
      return chain.proceed(requestWithEwt)
    }
    return chain.proceed(chain.request())
  }

  private fun getEwtAuthentication(): Single<String> {
    return walletService.getWalletAddress()
        .flatMap { getPayload(it) }
        .flatMap {
          walletService.signContent(it)
              .map { signedPayload -> "Bearer " + header.convertToBase64() + "." + it + "." + signedPayload }
        }
        .onErrorResumeNext { Single.just("Error") }
  }

  private fun getPayload(walletAddress: String): Single<String> {
    val unixTime = System.currentTimeMillis() / 1000L + 3600
    val payloadJson = JsonObject()
    payloadJson.addProperty("iss", walletAddress)
    payloadJson.addProperty("exp", unixTime)
    return Single.just(payloadJson.toString()
        .convertToBase64())
  }
}