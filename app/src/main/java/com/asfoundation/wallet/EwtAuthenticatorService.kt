package com.asfoundation.wallet

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.util.convertToBase64
import com.google.gson.JsonObject


private const val TTL = 3600

class EwtAuthenticatorService(private val walletService: WalletService,
                              private val header: String) {

  private var cachedAuth: MutableMap<String, Pair<String, Long>> = HashMap()

  @Synchronized
  fun getEwtAuthentication(address: String, currentUnixTime: Long): String {
    return if (shouldBuildEwtAuth(address, currentUnixTime))
      getNewEwtAuthentication(address, currentUnixTime)
    else {
      cachedAuth[address]!!.first
    }
  }

  @Synchronized
  fun getNewEwtAuthentication(address: String, currentUnixTime: Long): String {
    val payload = getPayload(address, currentUnixTime)
    val signedPayload = walletService.signContent(payload)
        .blockingGet()
    return buildAndSaveEwtString(header.convertToBase64(), payload, signedPayload, address,
        currentUnixTime)
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

  private fun getPayload(walletAddress: String, currentUnixTime: Long): String {
    val payloadJson = JsonObject()
    cachedAuth[walletAddress] = Pair("", currentUnixTime)
    payloadJson.addProperty("iss", walletAddress)
    payloadJson.addProperty("exp", currentUnixTime + TTL)
    return payloadJson.toString()
        .convertToBase64()
  }
}