package com.sw.writer;

import com.sw.domain.Customer;
import com.sw.repository.Person;
import com.sw.repository.PersonRepoImpl;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ComplementItemWriter implements ItemWriter<Customer> {

    private final PersonRepoImpl personRepo;

    public ComplementItemWriter(final PersonRepoImpl personRepo) {
        this.personRepo = personRepo;
    }

    @Override
    public void write(List<? extends Customer> customers) {

        for (Customer customer : customers) {

            Person person = new Person();
            person.setUid(customer.getName());
            person.setMail(customer.getName());
            person.setDigitalId(customer.getUserID());
            person.setFullName(customer.getName());
            person.setLastName(customer.getDisplayName());
            //TODO - Requirement still in discussion
            person.setInetuserstatus("Active");

            personRepo.create(person);
        }
    }
}
