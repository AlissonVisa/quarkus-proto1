package com.alissonvisa.domain.person;

import org.bson.types.ObjectId;

public interface Entity<T> extends CloneableEntity<T> {

    void restoreFromDatabase(ObjectId id);

    T find(ObjectId id);

    void persist();

    void update();

    void persistOrUpdate();

    void delete();

}
