package com.asfoundation.wallet.billing;

import com.appcoins.wallet.bdsbilling.repository.RemoteRepository;
import com.appcoins.wallet.bdsbilling.repository.entity.Transaction;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.math.BigDecimal;

public final class BDSTransactionService implements TransactionService {

  private final RemoteRepository remoteRepository;

  public BDSTransactionService(RemoteRepository remoteRepository) {
    this.remoteRepository = remoteRepository;
  }

  @Override
  public Single<String> createTransaction(String token, String packageName, String payload,
      String productName, String developerWallet, String storeWallet, String oemWallet,
      String origin, BigDecimal priceValue, String priceCurrency, String type, String callback,
      String orderReference, String referrerUrl) {
    return remoteRepository.createAdyenTransaction(origin, token, packageName, priceValue,
        priceCurrency, productName, type, developerWallet, storeWallet, oemWallet, payload,
        callback, orderReference, referrerUrl)
        .map(Transaction::getUid)
        .subscribeOn(Schedulers.io());
  }

  @Override public Single<String> getSession(String transactionUid) {
    return remoteRepository.getSessionKey(transactionUid)
        .map(authorization -> authorization.getData()
            .getSession())
        .subscribeOn(Schedulers.io());
  }

  @Override public Completable finishTransaction(String transactionUid, String paykey) {
    return remoteRepository.patchTransaction(transactionUid, paykey)
        .subscribeOn(Schedulers.io());
  }
}
