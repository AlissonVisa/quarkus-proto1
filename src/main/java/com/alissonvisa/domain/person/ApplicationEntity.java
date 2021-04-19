package com.alissonvisa.domain.person;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.runtime.JavaMongoOperations;
import org.bson.types.ObjectId;

public abstract class ApplicationEntity<T extends Entity> extends PanacheMongoEntity implements Entity<T>, Instantiable<T> {

    public ApplicationEntity() {}

    public void persist(){
        JavaMongoOperations.INSTANCE.persist(getEntity());
    }

    public void update() {
        JavaMongoOperations.INSTANCE.update(getEntity());
    }

    public void persistOrUpdate() {
        JavaMongoOperations.INSTANCE.persistOrUpdate(getEntity());
    }

    public void delete() {
        JavaMongoOperations.INSTANCE.delete(getEntity());
    }

    private T getEntity() {
        T entity = newInstance();
        entity.clone(this);
        return entity;
    }

    @Override
    public void restoreFromDatabase(ObjectId id) {
        this.clone(this.find(id));
    }

}
