package com.asfoundation.wallet.repository;

import android.util.Log;
import com.asfoundation.wallet.entity.TransactionBuilder;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by trinkes on 3/16/18.
 */

public class ApproveService {
  private final WatchedTransactionService transactionService;
  private final TransactionValidator approveTransactionSender;
  private final Map<String, Transaction> cenas;
  private final PublishSubject<List<Transaction>> dummyTransactionsRelay;

  public ApproveService(WatchedTransactionService transactionService,
      TransactionValidator approveTransactionSender) {
    this.transactionService = transactionService;
    this.approveTransactionSender = approveTransactionSender;
    this.cenas = new HashMap<>();
    this.dummyTransactionsRelay = PublishSubject.create();
  }

  public void start() {
    transactionService.start();
  }

  public Completable approveWithoutValidation(String key, TransactionBuilder transactionBuilder) {
    return transactionService.sendTransaction(key, transactionBuilder);
  }

  public Completable approve(String key, PaymentTransaction paymentTransaction) {
    return approveTransactionSender.validate(paymentTransaction)
        .flatMapCompletable(hash -> transactionService.sendTransaction(key,
            paymentTransaction.getTransactionBuilder()));
  }

  public Observable<ApproveTransaction> getApprove(String uri) {
    return transactionService.getTransaction(uri)
        .map(this::map);
  }

  private ApproveTransaction map(Transaction transaction) {
    return new ApproveTransaction(transaction.getKey(),
        mapTransactionState(transaction.getStatus()), transaction.getTransactionHash());
  }

  private Status mapTransactionState(Transaction.Status status) {
    Status toReturn;
    switch (status) {
      case PENDING:
        toReturn = Status.PENDING;
        break;
      case PROCESSING:
        toReturn = Status.APPROVING;
        break;
      case COMPLETED:
        toReturn = Status.APPROVED;
        break;
      default:
      case ERROR:
        toReturn = Status.ERROR;
        break;
      case WRONG_NETWORK:
        toReturn = Status.WRONG_NETWORK;
        break;
      case NONCE_ERROR:
        toReturn = Status.NONCE_ERROR;
        break;
      case UNKNOWN_TOKEN:
        toReturn = Status.UNKNOWN_TOKEN;
        break;
      case NO_TOKENS:
        toReturn = Status.NO_TOKENS;
        break;
      case NO_ETHER:
        toReturn = Status.NO_ETHER;
        break;
      case NO_FUNDS:
        toReturn = Status.NO_FUNDS;
        break;
      case NO_INTERNET:
        toReturn = Status.NO_INTERNET;
        break;
    }
    return toReturn;
  }

  public Observable<List<ApproveTransaction>> getAll() {
    // Combine latest with dummy
    // Status approved
    // key, payment transaction como argumentos

    //Observable<List<Transaction>> just = Observable.just(cenas);
    Observable<List<Transaction>> just =
        dummyTransactionsRelay.startWith(new LinkedList<Transaction>())
            .doOnNext(transactions -> Log.d(TAG, "onNext cache: " + transactions));
    Observable<List<Transaction>> all = transactionService.getAll()
        .startWith(new LinkedList<Transaction>())
        .doOnNext(transactions -> Log.d(TAG, "onNext transactionService.getAll: " + transactions));
    Observable<List<Transaction>> combineLatest =
        Observable.combineLatest(all, just, (transactions, transactions2) -> {
          Log.d(TAG, "transactionService.getAll: " + transactions);
          Log.d(TAG, "cache: " + transactions2);
          List<Transaction> list = new LinkedList<>();
          list.addAll(transactions);
          list.addAll(cenas.values());

          return list;
        });
    return combineLatest.flatMapSingle(transactions -> Observable.fromIterable(transactions)
        .map(this::map)
        .toList());
    //return transactionService.getAll()
    //    .flatMapSingle(transactions -> Observable.fromIterable(transactions)
    //        .map(this::map)
    //        .toList());
  }

  private static final String TAG = ApproveService.class.getSimpleName();

  public Completable remove(String key) {
    return transactionService.remove(key)
        .andThen(Completable.fromAction(() -> {
          cenas.remove(key);
          ArrayList<Transaction> t = new ArrayList<>(cenas.values());
          Log.d(TAG, "remove emmited list: " + t);
          dummyTransactionsRelay.onNext(t);
        }));
  }

  public CompletableSource approveDummy(String key, PaymentTransaction paymentTransaction) {
    //new Transaction(key, Transaction.Status.COMPLETED, paymentTransaction.getTransactionBuilder
    // (), "0x");
    //return Completable.fromRunnable(() -> cenas.add(paymentTransaction));

    return approveTransactionSender.validate(paymentTransaction)
        .flatMapCompletable(hash -> Completable.fromRunnable(() -> cenas.put(key,
            new Transaction(key, Transaction.Status.COMPLETED,
                paymentTransaction.getTransactionBuilder(), hash)))
            .andThen(Completable.fromRunnable(
                () -> dummyTransactionsRelay.onNext(new ArrayList<>(cenas.values())))));
  }

  public enum Status {
    PENDING, APPROVING, APPROVED, ERROR, WRONG_NETWORK, NONCE_ERROR, UNKNOWN_TOKEN, NO_TOKENS,
    NO_ETHER, NO_FUNDS, NO_INTERNET
  }

  public class ApproveTransaction {
    private final String key;
    private final Status status;
    private final String transactionHash;

    public ApproveTransaction(String key, Status status, String transactionHash) {
      this.key = key;
      this.status = status;
      this.transactionHash = transactionHash;
    }

    public String getKey() {
      return key;
    }

    public Status getStatus() {
      return status;
    }

    public String getTransactionHash() {
      return transactionHash;
    }
  }
}
