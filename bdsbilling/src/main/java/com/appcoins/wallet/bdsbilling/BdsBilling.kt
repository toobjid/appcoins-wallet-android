package com.appcoins.wallet.bdsbilling

import com.appcoins.wallet.bdsbilling.repository.BillingSupportedType
import com.appcoins.wallet.bdsbilling.repository.entity.PaymentMethodEntity
import com.appcoins.wallet.bdsbilling.repository.entity.Purchase
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction
import com.appcoins.wallet.billing.repository.entity.Product
import io.reactivex.Scheduler
import io.reactivex.Single

class BdsBilling(private val repository: BillingRepository,
                 private val walletService: WalletService,
                 private val errorMapper: BillingThrowableCodeMapper) : Billing {
  override fun getWallet(packageName: String): Single<String> {
    return repository.getWallet(packageName)
  }

  override fun isInAppSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.INAPP)
        .map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun isSubsSupported(merchantName: String): Single<Billing.BillingSupportType> {
    return repository.isSupported(merchantName, BillingSupportedType.SUBS)
        .map { map(it) }
        .onErrorReturn { errorMapper.map(it) }
  }

  override fun getProducts(merchantName: String, skus: List<String>): Single<List<Product>> {
    return repository.getSkuDetails(merchantName, skus)
  }

  override fun getAppcoinsTransaction(uid: String, scheduler: Scheduler): Single<Transaction> {
    return repository.getAppcoinsTransaction(uid)
        .subscribeOn(scheduler)
  }

  override fun getSkuTransaction(merchantName: String, sku: String?,
                                 scheduler: Scheduler): Single<Transaction> {
    return repository.getSkuTransaction(merchantName, sku)
        .subscribeOn(scheduler)
  }

  override fun getSkuPurchase(merchantName: String, sku: String?,
                              scheduler: Scheduler): Single<Purchase> {
    return repository.getSkuPurchase(merchantName, sku)
        .subscribeOn(scheduler)
  }

  override fun getPurchases(merchantName: String, type: BillingSupportedType,
                            scheduler: Scheduler): Single<List<Purchase>> {
    return repository.getPurchases(merchantName, type)
        .subscribeOn(scheduler)
        .onErrorReturn { ArrayList() }
  }

  override fun consumePurchases(merchantName: String, purchaseToken: String,
                                scheduler: Scheduler): Single<Boolean> {
    return repository.consumePurchases(merchantName, purchaseToken)
        .subscribeOn(scheduler)
        .onErrorReturn { false }
  }

  override fun getPaymentMethods(value: String,
                                 currency: String): Single<List<PaymentMethodEntity>> {
    return repository.getPaymentMethods(value, currency)
        .onErrorReturn {
          it.printStackTrace()
          ArrayList()
        }
  }

  private fun map(it: Boolean) =
      if (it) Billing.BillingSupportType.SUPPORTED else Billing.BillingSupportType.MERCHANT_NOT_FOUND


}