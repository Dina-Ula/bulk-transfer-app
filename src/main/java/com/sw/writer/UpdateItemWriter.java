package com.sw.writer;

import com.sw.domain.Customer;
import com.sw.repository.Person;
import com.sw.repository.PersonRepo;
import com.sw.repository.PersonRepoImpl;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;

@Component
public class UpdateItemWriter implements ItemWriter<Customer> {

    private final PersonRepo personRepo;

    public UpdateItemWriter(final PersonRepoImpl personRepo) {
        this.personRepo = personRepo;
    }

    @Override
    public void write(List<? extends Customer> customers) {

        for (Customer customer : customers) {

            if (!isEmpty(customer.getOldName())) {
                personRepo.rename(customer.getOldName(), customer.getName());
            }

            Person person = new Person();
            person.setUid(customer.getName());
            person.setMail(customer.getName());
            person.setDigitalId(customer.getUserID());
            person.setFullName(customer.getName());
            person.setLastName(customer.getDisplayName());

            personRepo.update(person);
        }
    }
}
