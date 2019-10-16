package com.asfoundation.wallet.repository.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(tableName = "tokens", primaryKeys = {"walletAddress","tokenAddress" })
public class TokensEntity{
    @NonNull public String networkInfo;
    @NonNull public String walletAddress;
    @NonNull public String tokenAddress;

    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "symbol")
    public String symbol;
    @ColumnInfo(name = "decimals")
    public int decimals;
    @ColumnInfo(name = "added_time")
    public Long addedTime;
    @ColumnInfo(name = "updated_time")
    public Long updatedTime;
    @ColumnInfo(name = "balance")
    public String balance;
    @ColumnInfo(name = "is_enabled")
    public Boolean isEnabled;
    @ColumnInfo(name = "is_added_manually")
    public Boolean isAddedManually;
    // don't know if it can be done with the obejct token
  /*  @ColumnInfo(name = "token_object")
    public Token tokenObject;

    public Token getToken() {
        return tokenObject;
    }
    public void setToken(Token tokenObject) {
        this.tokenObject = tokenObject;
    }
*/

    public String getNetworkInfo() {
        return networkInfo;
    }

    public void setNetworkInfo(String networkInfo) {
        this.networkInfo = networkInfo;
    }

    public String getWalletAddress() {
        return walletAddress;
    }

    public void setWalletAddress(String wallet) {
        this.walletAddress = wallet;
    }

    public String getName() { return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public Long getAddedTime() {
        return addedTime;
    }

    public void setAddedTime(Long addedTime) {
        this.addedTime = addedTime;
    }

    public Long getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Long updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public Boolean getisEnabled() {
        return isEnabled;
    }

    public void setIsEnabled(Boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public Boolean getIsAddedManually() {
        return isAddedManually;
    }

    public void setIsAddedManually(Boolean isAddedManually) {
        this.isAddedManually = isAddedManually;
    }
}

