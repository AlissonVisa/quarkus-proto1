package com.alissonvisa.domain.person;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@SuperBuilder
public class Address {

    private ObjectId addressId;
    private String street;
    private Integer number;
    private AddressType addressType;
    private String city;

}
