package com.alissonvisa.base.persistence;

import com.mongodb.client.MongoClient;
import org.bson.types.ObjectId;

import java.util.UUID;

public interface Entity<T> {

    void restoreFromDatabase(ObjectId id, MongoClient mongoClient, String database);

    ObjectId getId();

    void setId(ObjectId id);

    UUID lockId();

    void lockId(UUID lockId);

    void persist();

    void update();

    void persistOrUpdate();

    void delete();

}
