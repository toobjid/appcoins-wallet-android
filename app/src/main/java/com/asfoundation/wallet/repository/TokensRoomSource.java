package com.asfoundation.wallet.repository;

import android.text.format.DateUtils;
import com.asfoundation.wallet.entity.NetworkInfo;
import com.asfoundation.wallet.entity.Token;
import com.asfoundation.wallet.entity.TokenInfo;
import com.asfoundation.wallet.entity.TokenTicker;
import com.asfoundation.wallet.entity.Wallet;
import com.asfoundation.wallet.repository.entity.TokensDao;
import com.asfoundation.wallet.repository.entity.TokensEntity;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class TokensRoomSource implements TokenLocalSource {

  private static final long ACTUAL_BALANCE_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS;
  private static final long ACTUAL_TOKEN_TICKER_INTERVAL = 5 * DateUtils.MINUTE_IN_MILLIS;
  private static final String COINMARKETCAP_IMAGE_URL =
      "https://files.coinmarketcap.com/static/img/coins/128x128/%s.png";
  private final TokensDao roomDatabase;

  public TokensRoomSource(TokensDao roomDatabase) {
    this.roomDatabase = roomDatabase;
  }

  @Override public Completable saveTokens(NetworkInfo networkInfo, Wallet wallet, Token[] items) {
    return Completable.fromAction(() -> {
      Date now = new Date();
      for (Token token : items) {
        saveToken(networkInfo, wallet, token, now);
      }
    });
  }

  @Override public void updateTokenBalance(NetworkInfo network, Wallet wallet, Token token) {
    List<TokensEntity> tokenList = roomDatabase.getAllTokens(network.name);
    int index = tokenList.indexOf(token.tokenInfo.address);
    TokensEntity tokenEntity = new TokensEntity();
    tokenEntity = tokenList.get(index);
    roomDatabase.delete(tokenEntity);
    if (token.balance!=null)
      tokenEntity.balance = token.balance.toString();
    roomDatabase.insertAll(tokenEntity);
  }

  @Override
  public void setEnable(NetworkInfo network, Wallet wallet, Token token, boolean isEnabled) {
    List<TokensEntity> tokenList = roomDatabase.getAllTokens(network.name);
    int index = tokenList.indexOf(token.tokenInfo.address);
    TokensEntity tokenEntity = new TokensEntity();
    tokenEntity = tokenList.get(index);
    roomDatabase.delete(tokenEntity);
    tokenEntity.isEnabled = token.tokenInfo.isEnabled;
    roomDatabase.insertAll(tokenEntity);
  }

  @Override public Single<Token[]> fetchEnabledTokens(NetworkInfo networkInfo, Wallet wallet) {
    return Single.fromCallable(() -> {
      List<TokensEntity> tokenList = roomDatabase.getAllTokens(networkInfo.name);
      int index = 0;
      for (TokensEntity token : tokenList) {
        if (token.isEnabled == false){

          tokenList.remove(index);
          index++;

        }
      }
      return convert(tokenList, System.currentTimeMillis());
    });
  }

  @Override public Single<Token[]> fetchAllTokens(NetworkInfo networkInfo, Wallet wallet) {
    return Single.fromCallable(() -> {
      Thread.sleep(100);
      List<TokensEntity> tokenList = roomDatabase.getAllTokens(networkInfo.name);
      return convert(tokenList, System.currentTimeMillis());
    });
  }

  @Override
  public Completable saveTickers(NetworkInfo network, Wallet wallet, TokenTicker[] tokenTickers) {
    return Completable.complete();
  }

  @Override
  public Single<TokenTicker[]> fetchTickers(NetworkInfo network, Wallet wallet, Token[] tokens) {
    return Single.fromCallable(() -> new TokenTicker[0]);
  }

  @Override public Completable delete(NetworkInfo network, Wallet wallet, Token token) {
    return Completable.fromAction(() -> {

      List<TokensEntity> tokenList = roomDatabase.getAllTokens(network.name);
      int index = tokenList.indexOf(token.tokenInfo.address);
      TokensEntity tokenEntity = new TokensEntity();
      tokenEntity = tokenList.get(index);
      roomDatabase.delete(tokenEntity);
      Realm realm = null;
    })
        .subscribeOn(Schedulers.io());
  }

  private void saveToken(NetworkInfo networkInfo, Wallet wallet, Token token, Date currentTime) {

    Completable.fromAction(() -> {
      TokensEntity tokenEntity = new TokensEntity();
      tokenEntity.networkInfo = networkInfo.name;
      tokenEntity.walletAddress= wallet.address;
      tokenEntity.tokenAddress = token.tokenInfo.address;
      tokenEntity.name = token.tokenInfo.name;
      tokenEntity.symbol = token.tokenInfo.symbol;
      tokenEntity.decimals = token.tokenInfo.decimals;
      tokenEntity.addedTime = currentTime.getTime();
      tokenEntity.updatedTime = currentTime.getTime();
      tokenEntity.balance = token.balance == null ? null : token.balance.toString();
      tokenEntity.isEnabled = true;
      tokenEntity.isAddedManually = token.tokenInfo.isAddedManually;
      roomDatabase.insertAll(tokenEntity);
    })
        .subscribeOn(Schedulers.io())
        .subscribe();
  }


  private Token[] convert(List<TokensEntity> roomItems, long now) {
    int len = roomItems.size();
    Token[] result = new Token[len];
    for (int i = 0; i < len; i++) {
      TokensEntity roomItem = roomItems.get(i);
      if (roomItem != null) {
        TokenInfo info = new TokenInfo(roomItem.tokenAddress, roomItem.name, roomItem.symbol,
            roomItem.decimals, roomItem.isEnabled, roomItem.isAddedManually);
        BigDecimal balance;
        if(roomItem.balance!=null)
          balance = new BigDecimal(roomItem.balance);
        else
          balance = null;
        result[i] = new Token(info, balance, roomItem.updatedTime);
      }
    } return result;
  }
}