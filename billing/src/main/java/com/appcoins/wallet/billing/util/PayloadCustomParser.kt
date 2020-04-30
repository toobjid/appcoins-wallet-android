package com.appcoins.wallet.billing.util

import com.appcoins.wallet.billing.util.PayloadHelper.*

class PayloadCustomParser {

  fun getPayload(uriString: String): String {
    val indexOfPayload = uriString.indexOf(PAYLOAD_PARAMETER)

    if (indexOfPayload == -1) {
      return ""
    }

    val indexOfPayloadValue: Int = indexOfPayload + PAYLOAD_PARAMETER.length + 1

    val tokens = listOf(ORDER_PARAMETER, ORIGIN_PARAMETER)

    val value = tokens.groupBy { uriString.indexOf(it, indexOfPayloadValue) }
        .filter { it.key != -1 }
        .toSortedMap()
        .toList()
        .firstOrNull()
        ?.first

    return if (value != null) {
      uriString.substring(indexOfPayloadValue, value - 1)
    } else {
      uriString.substring(indexOfPayloadValue)
    }
  }
}