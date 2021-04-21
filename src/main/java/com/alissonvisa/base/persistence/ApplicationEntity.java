package com.alissonvisa.base.persistence;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.runtime.JavaMongoOperations;
import lombok.NoArgsConstructor;
import lombok.extern.jbosslog.JBossLog;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;

@JBossLog
@NoArgsConstructor
public abstract class ApplicationEntity extends PanacheMongoEntity implements Entity<Object>, Instantiable<Object> {

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

    private Object getEntity() {
        ModelMapper modelMapper = new ModelMapper();
        Object entity = newInstance();
        modelMapper.map(this, entity);
        return entity;
    }

    @Override
    public void restoreFromDatabase(ObjectId id) {
        this.clone(this.find(id));
    }

    @Override
    public void clone(Object sourceEntity) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(sourceEntity, this);
    }

    @Override
    public Object newInstance() {
        try {
            return Class.forName(this.getClass().getName().replace("_Subclass", ""))
                    .getDeclaredConstructors()[0]
                    .newInstance();
        } catch (Exception e) {
            log.error("m=newInstance " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
