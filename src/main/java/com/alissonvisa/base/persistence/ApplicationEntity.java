package com.alissonvisa.base.persistence;

import com.alissonvisa.base.exception.EntityProcessTimeoutException;
import com.alissonvisa.base.json.ObjectIdDeserializer;
import com.alissonvisa.base.persistence.lock.EntityLockManager;
import com.alissonvisa.util.TimeWatch;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.runtime.JavaMongoOperations;
import lombok.extern.jbosslog.JBossLog;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.modelmapper.ModelMapper;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Filters.eq;

@JBossLog
public abstract class ApplicationEntity extends PanacheMongoEntity implements Entity<Object> {

    @Inject
    EntityLockManager lockManager;

    @ConfigProperty(name = "application.entity-locker.entity-process-timeout")
    Long entityProcessTimeout = 4000L;

    @ConfigProperty(name = "application.entity-locker.enabled", defaultValue = "true")
    Boolean entityLockerEnabled;

    private TimeWatch timeWatch;

    private UUID lockId;

    protected Boolean active = Boolean.TRUE;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public UUID lockId() {
        return this.lockId;

    }

    @Override
    public void lockId(UUID lockId) {
        this.lockId = lockId;
        this.timeWatch = TimeWatch.start();
    }

    public void persist(){
        JavaMongoOperations.INSTANCE.persist(entity());
    }

    public void update() {
        JavaMongoOperations.INSTANCE.update(entity());
    }

    public void persistOrUpdate() {
        JavaMongoOperations.INSTANCE.persistOrUpdate(entity());
    }

    public void delete() {
        this.active = Boolean.FALSE;
//        JavaMongoOperations.INSTANCE.delete(entity());
    }

    private Object entity() {
        ModelMapper modelMapper = new ModelMapper();
        Object entity = newInstance();
        modelMapper.map(this, entity);
        return entity;
    }

    @Override
    public void restoreFromDatabase(ObjectId id, MongoClient mongoClient, String database) {
        this.clone(this.find(id, mongoClient, database));
    }

    private void clone(Object sourceEntity) {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.map(sourceEntity, this);
    }

    private Object newInstance() {
        try {
            return clazz().getDeclaredConstructors()[0].newInstance();
        } catch (Exception e) {
            log.error("m=newInstance " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    private Object find(ObjectId id, MongoClient mongoClient, String database) {
        MongoCollection<Document> collection = mongoClient.getDatabase(database).getCollection(simpleClassName());
        Document document = collection.find(eq("_id", id)).first();
        try {
            return objectMapper().readValue(document.toJson().replace("_id", "id"), clazz());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> clazz() {
        try {
            return Class.forName(completeClassName());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule mod = new SimpleModule("ObjectId");
        mod.addDeserializer(ObjectId.class, new ObjectIdDeserializer());
        mapper.registerModule(mod);
        return mapper;
    }

    public String completeClassName() {
        return this.getClass().getPackageName()
                + "."
                + this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().indexOf("_"));
    }

    public String simpleClassName() {
        return this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().indexOf("_"));
    }

    @PreDestroy
    public void preDestroy() {
        if(entityLockerEnabled == Boolean.FALSE) {
            this.persistOrUpdate();
        } else if (this.timeWatch.time(TimeUnit.MILLISECONDS) < this.entityProcessTimeout) {
            this.persistOrUpdate();
            this.lockManager.removeLock(this);
            log.info("entity hash " + this.hashCode());
            log.info("entity processed id " + this.getId().toHexString() + " elapsed time " + this.timeWatch.time(TimeUnit.SECONDS) + " seconds.");
        } else {
            throw new EntityProcessTimeoutException("Entity process timeout. EntityId = " + this.getId() + " elapsed time = " + timeWatch.time(TimeUnit.SECONDS) + " seconds.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationEntity that = (ApplicationEntity) o;
        return Objects.equals(active, that.active);
    }

    @Override
    public int hashCode() {
        return Objects.hash(active);
    }
}
