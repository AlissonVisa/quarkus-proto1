package com.alissonvisa.base.persistence;

import com.alissonvisa.base.json.ObjectIdDeserializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.runtime.JavaMongoOperations;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jbosslog.JBossLog;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.modelmapper.ModelMapper;

import javax.annotation.PreDestroy;

import static com.mongodb.client.model.Filters.eq;

@JBossLog
@Getter
@Setter
public abstract class ApplicationEntity extends PanacheMongoEntity implements Entity<Object> {

    protected Boolean active = Boolean.TRUE;

    public ObjectId getId() {
        return this.id;
    }

    public void setId(ObjectId id) {
        this.id = id;
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

    private String completeClassName() {
        return this.getClass().getPackageName()
                + "."
                + this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().indexOf("_"));
    }

    private String simpleClassName() {
        return this.getClass().getSimpleName().substring(0, this.getClass().getSimpleName().indexOf("_"));
    }

    @PreDestroy
    public void preDestroy() {
        this.persistOrUpdate();
    }

}
