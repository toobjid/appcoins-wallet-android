package com.asfoundation.wallet

import com.appcoins.wallet.bdsbilling.WalletService
import com.asfoundation.wallet.util.convertToBase64
import com.google.gson.JsonObject

/**
 * Represents the time until we need to request another ewt token.
 * It's represented in seconds
 * **/
private const val TTL = 3600

class EwtAuthenticatorService(private val walletService: WalletService,
                              private val header: String) {

  private var cachedAuth: MutableMap<String, Pair<String, Long>> = HashMap()

  @Synchronized
  fun getEwtAuthentication(address: String): String {
    return if (shouldBuildEwtAuth(address))
      getNewEwtAuthentication(address)
    else {
      cachedAuth[address]!!.first
    }
  }

  @Synchronized
  fun getNewEwtAuthentication(address: String): String {
    val currentUnixTime = System.currentTimeMillis() / 1000L
    val ewtString = buildEwtString(address, currentUnixTime)
    cachedAuth[address] = Pair(ewtString, currentUnixTime + TTL)
    return ewtString
  }

  private fun shouldBuildEwtAuth(address: String): Boolean {
    val currentUnixTime = System.currentTimeMillis() / 1000L
    return !cachedAuth.containsKey(address) || hasExpired(currentUnixTime,
        cachedAuth[address]?.second)
  }

  private fun hasExpired(currentUnixTime: Long, ttlUnixTime: Long?): Boolean {
    return ttlUnixTime == null || currentUnixTime >= ttlUnixTime
  }

  private fun buildEwtString(address: String, currentUnixTime: Long): String {
    val header = replaceInvalidCharacters(header.convertToBase64())
    val payload = replaceInvalidCharacters(getPayload(address, currentUnixTime))
    val signedPayload = walletService.signContent(payload)
        .blockingGet()
    return "Bearer $header.$payload.$signedPayload"
  }

  private fun getPayload(walletAddress: String, currentUnixTime: Long): String {
    val payloadJson = JsonObject()
    cachedAuth[walletAddress] = Pair("", currentUnixTime)
    payloadJson.addProperty("iss", walletAddress)
    payloadJson.addProperty("exp", currentUnixTime + TTL)
    return payloadJson.toString()
        .convertToBase64()
  }

  private fun replaceInvalidCharacters(ewtString: String): String {
    return ewtString.replace("=", "")
        .replace("+", "-")
        .replace("/", "_")
  }
}