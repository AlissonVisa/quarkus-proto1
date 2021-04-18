package com.alissonvisa.domain.person;

import com.alissonvisa.messaging.CommandType;
import com.alissonvisa.messaging.CreatePersonCommand;
import org.jboss.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Objects;
import java.util.UUID;

@Named
@Dependent
public class Person implements CloneableEntity<Person> {

    private static final Logger LOG = Logger.getLogger(Person.class);

    @Inject
    private PersonMapper personMapper;

    @MessageHandler
    public void handle(@Observes @CommandType CreatePersonCommand command) {
        LOG.info(this.toString());
        this.name = command.getPayload().getName();
        this.id = command.getPayload().getId();
        LOG.info(command.toString());
        createId();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            LOG.error(e.getMessage(),e);
        }
        LOG.info(this.toString());
    }

    UUID id;
    String name;

    public Person(String name) {
        this.name = name;
    }

    private Person() {}

    private void createId() {
        this.id = UUID.randomUUID();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
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
        this.id = sourceEntity.getId();
        this.name = sourceEntity.getName();
    }
}
