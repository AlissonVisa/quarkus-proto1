package com.alissonvisa.domain.person;

import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface PersonMapper {
    Person clone(Person person);
}
