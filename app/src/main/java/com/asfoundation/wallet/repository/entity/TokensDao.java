package com.asfoundation.wallet.repository.entity;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface TokensDao {

    //@Query("SELECT * FROM tokens where networkInfo LIKE  :networkInfo AND wallet LIKE :wallet ORDER BY added_time")
    @Query("SELECT * FROM tokens where networkInfo LIKE  :networkInfo ORDER BY added_time")
    List<TokensEntity> getAllTokens(String networkInfo);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(TokensEntity tokensEntity);

    @Delete
    void delete(TokensEntity tokenEntity);
}
