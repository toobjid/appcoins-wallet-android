package com.asfoundation.wallet.billing.purchase

import com.asfoundation.wallet.billing.share.GetPaymentLinkResponse
import com.google.gson.annotations.SerializedName
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.POST

class LocalPayementsLinkRepository(private var api: DeepLinkApi) : InAppDeepLinkRepository {

  override fun getDeepLink(domain: String, skuId: String?,
                           originalAmount: String?, originalCurrency: String?,
                           paymentMethod: String,
                           developerWalletAddress: String,
                           storeWalletAddress: String, oemWalletAddress: String,
                           callbackUrl: String?, orderReference: String?,
                           payload: String?): Single<String> {
    return api.getDeepLink(
        DeepLinkData(domain, skuId, null, originalAmount,
            originalCurrency,
            paymentMethod, developerWalletAddress, callbackUrl, payload, orderReference,
            storeWalletAddress, oemWalletAddress))
        .map { it.url }
  }

  interface DeepLinkApi {

    @POST("deeplink/8.20191003/inapp/product/purchases")
    fun getDeepLink(@Body data: DeepLinkData): Single<GetPaymentLinkResponse>
  }
}

data class DeepLinkData(@SerializedName("package") var packageName: String,
                        var sku: String?,
                        var message: String?, @SerializedName("price.value")
                        var amount: String?, @SerializedName("price.currency")
                        var currency: String?, var method: String,
                        @SerializedName("wallets.developer") var developerWalletAddress: String,
                        @SerializedName("callback_url") var callback: String?,
                        var metadata: String?, var reference: String?,
                        @SerializedName("wallets.store") var storeWalletAddress: String,
                        @SerializedName("wallets.oem") var oemWalletAddress: String?)
