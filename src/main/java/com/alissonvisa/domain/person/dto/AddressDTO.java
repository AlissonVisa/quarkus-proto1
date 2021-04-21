package com.alissonvisa.domain.person.dto;

import com.alissonvisa.domain.person.Address;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AddressDTO extends Address {

    private String personId;

}
