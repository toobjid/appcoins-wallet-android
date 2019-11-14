package com.asfoundation.wallet.billing;

import io.reactivex.Completable;
import io.reactivex.Single;
import java.math.BigDecimal;

public interface TransactionService {

  Single<String> createTransaction(String token, String packageName, String payload,
      String productName, String developerWallet, String storeWallet, String oemWallet,
      String origin, String walletAddress, BigDecimal priceValue, String priceCurrency, String type,
      String callback, String orderReference, String referrerUrl);

  Single<String> getSession(String transactionUid);

  Completable finishTransaction(String transactionUid, String paykey);
}
