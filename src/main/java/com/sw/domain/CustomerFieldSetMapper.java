package com.sw.domain;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;

import static com.sw.domain.Constants.Customer.Attribute.DISPLAY_NAME;
import static com.sw.domain.Constants.Customer.Attribute.NAME;
import static com.sw.domain.Constants.Customer.Attribute.USER_ID;

public class CustomerFieldSetMapper implements FieldSetMapper<Customer> {

    @Override
    public Customer mapFieldSet(FieldSet fieldSet) {
        return new Customer(fieldSet.readString(NAME),
                fieldSet.readString(USER_ID),
                fieldSet.readString(DISPLAY_NAME));
    }
}
