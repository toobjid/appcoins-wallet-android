package com.asfoundation.wallet.repository.entity;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {TokensEntity.class}, version = 1)
public abstract class TokensRoomDatabase extends RoomDatabase {
    public abstract TokensDao tokensDao();

}