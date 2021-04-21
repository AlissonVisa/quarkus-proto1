package com.alissonvisa.base.persistence;

import com.mongodb.client.MongoClient;
import org.bson.types.ObjectId;

public interface Entity<T> {

    void restoreFromDatabase(ObjectId id, MongoClient mongoClient, String database);

    void persist();

    void update();

    void persistOrUpdate();

    void delete();

}
