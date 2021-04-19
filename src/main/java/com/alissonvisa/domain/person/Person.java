package com.alissonvisa.domain.person;

import com.alissonvisa.messaging.CommandType;
import com.alissonvisa.messaging.CreatePersonCommand;
import com.alissonvisa.messaging.UpdatePersonNameCommand;
import io.quarkus.mongodb.panache.MongoEntity;
import org.bson.types.ObjectId;
import org.jboss.logging.Logger;

import javax.enterprise.event.Observes;
import java.util.Objects;

@MongoEntity(collection = "PERSON")
public class Person extends ApplicationEntity<Person> {

    private static final Logger LOG = Logger.getLogger(Person.class);

    private ObjectId id;
    private String name;

    public Person() {}

    public Person(String name) {
        this.name = name;
    }

    public Person(ObjectId id, String name) {
        this.id = id;
        this.name = name;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return Objects.equals(id, person.id) &&
                Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public void clone(Person sourceEntity) {
        if(sourceEntity == null) return;
        this.setId(sourceEntity.getId());
        this.setName(sourceEntity.getName());
    }

    public void handle(@Observes @CommandType CreatePersonCommand command) {
        this.setName(command.getPayload().getName());
        this.persist();
    }

    @Stateful
    public void handle(@Observes @CommandType UpdatePersonNameCommand command) {
        this.setName(command.getPayload().getName());
        this.update();
    }

    @Override
    public Person newInstance() {
        return new Person();
    }

    @Override
    public Person find(ObjectId id) {
        return Person.findById(id);
    }
}
