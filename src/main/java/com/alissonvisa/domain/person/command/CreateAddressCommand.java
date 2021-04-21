package com.alissonvisa.domain.person.command;

import com.alissonvisa.base.messaging.ApplicationCommand;
import com.alissonvisa.domain.person.AddressType;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

@Data
@Builder
public class CreateAddressCommand extends ApplicationCommand {

    private ObjectId id;
    private String street;
    private Integer number;
    private AddressType addressType;
    private String city;

}
