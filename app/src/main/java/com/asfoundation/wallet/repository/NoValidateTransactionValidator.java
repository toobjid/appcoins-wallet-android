package com.asfoundation.wallet.repository;

import io.reactivex.Completable;
import io.reactivex.Single;

public class NoValidateTransactionValidator implements TransactionValidator {

  public NoValidateTransactionValidator() {
  }

  @Override public Single<String> validate(PaymentTransaction paymentTransaction) {
    return Completable.complete()
        .andThen(Single.just(""));
  }
}
