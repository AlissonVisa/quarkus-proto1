package com.alissonvisa.base.persistence;

public interface CloneableEntity<T> {

    void clone(T sourceEntity);

}
