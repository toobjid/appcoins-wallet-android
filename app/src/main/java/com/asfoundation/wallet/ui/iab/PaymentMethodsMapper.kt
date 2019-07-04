package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.ui.iab.PaymentMethodsView.SelectedPaymentMethod

class PaymentMethodsMapper {

  fun map(paymentId: String): SelectedPaymentMethod {
    return when (paymentId) {
      "ask_friend" -> SelectedPaymentMethod.SHARE_LINK
      "paypal" -> SelectedPaymentMethod.PAYPAL
      "credit_card" -> SelectedPaymentMethod.CREDIT_CARD
      "appcoins" -> SelectedPaymentMethod.APPC
      "appcoins_credits" -> SelectedPaymentMethod.APPC_CREDITS
      else -> SelectedPaymentMethod.LOCAL_PAYMENTS
    }
  }

  fun map(selectedPaymentMethod: SelectedPaymentMethod): String {
    return when (selectedPaymentMethod) {
      SelectedPaymentMethod.SHARE_LINK -> "ask_friend"
      SelectedPaymentMethod.PAYPAL -> "paypal"
      SelectedPaymentMethod.CREDIT_CARD -> "credit_card"
      SelectedPaymentMethod.APPC -> "appcoins"
      SelectedPaymentMethod.APPC_CREDITS -> "appcoins_credits"
      SelectedPaymentMethod.LOCAL_PAYMENTS -> "local_payments"
    }
  }
}