package com.asfoundation.wallet

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.util.convertToBase64
import com.google.gson.JsonObject
import io.reactivex.Single


private const val TTL = 3600

class EwtAuthenticatorService(private val walletService: WalletService,
                              private val header: String) {

  private var cachedAuth: MutableMap<String, Pair<String, Long>> = HashMap()

  fun getEwtAuthentication(address: String, currentUnixTime: Long): Single<String> {
    return if (shouldBuildEwtAuth(address, currentUnixTime))
      getPayload(address, currentUnixTime)
          .flatMap {
            walletService.signContent(it)
                .map { signedPayload ->
                  buildAndSaveEwtString(header.convertToBase64(), it, signedPayload, address,
                      currentUnixTime)
                }
          }
          .onErrorResumeNext { Single.just("Error") }
    else {
      Single.just(cachedAuth[address]!!.first)
    }
  }

  private fun shouldBuildEwtAuth(address: String, currentUnixTime: Long): Boolean {
    return cachedAuth[address] == null || hasExpired(currentUnixTime, cachedAuth[address]?.second)
  }

  private fun hasExpired(currentUnixTime: Long, ttlUnixTime: Long?): Boolean {
    return ttlUnixTime == null || currentUnixTime >= ttlUnixTime
  }

  private fun buildAndSaveEwtString(header: String, payload: String,
                                    signedPayload: String, address: String,
                                    currentUnixTime: Long): String {
    val ewtString = "Bearer $header.$payload.$signedPayload"
    cachedAuth[address] = Pair(ewtString, currentUnixTime + TTL)
    return ewtString
  }

  private fun getPayload(walletAddress: String, currentUnixTime: Long): Single<String> {
    val payloadJson = JsonObject()
    cachedAuth[walletAddress] = Pair("", currentUnixTime)
    payloadJson.addProperty("iss", walletAddress)
    payloadJson.addProperty("exp", currentUnixTime + TTL)
    return Single.just(payloadJson.toString()
        .convertToBase64())
  }
}