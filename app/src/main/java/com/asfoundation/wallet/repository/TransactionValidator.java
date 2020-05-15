package com.asfoundation.wallet.repository;

import io.reactivex.Single;

public interface TransactionValidator {
  Single<String> validate(PaymentTransaction paymentTransaction);
}
