package com.alissonvisa.domain.person;

public interface CloneableEntity<T> {

    void clone(T sourceEntity);

}
