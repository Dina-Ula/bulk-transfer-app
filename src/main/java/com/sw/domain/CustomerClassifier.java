package com.sw.domain;

import com.sw.config.ApplicationPropertiesConfig;
import com.sw.repository.Person;
import com.sw.repository.PersonRepoImpl;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.classify.Classifier;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.sw.repository.Constants.Person.Attribute.DIGITAL_ID;
import static com.sw.repository.Constants.Person.Attribute.MAIL;
import static org.apache.commons.lang3.BooleanUtils.isFalse;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

@Component
public class CustomerClassifier implements Classifier<Customer, ItemWriter<? super Customer>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerClassifier.class);

    private final PersonRepoImpl personRepo;
    private final ApplicationPropertiesConfig configuration;
    private final ItemWriter<Customer> updateItemWriter;
    private final ItemWriter<Customer> complementItemWriter;
    private final ItemWriter<Customer> swwExceptionItemWriter;
    private final ItemWriter<Customer> swaIntersectionItemWriter;

    public CustomerClassifier(final PersonRepoImpl personRepo, final ApplicationPropertiesConfig configuration,
                              final ItemWriter<Customer> complementItemWriter, final ItemWriter<Customer> updateItemWriter,
                              final ItemWriter<Customer> swwExceptionItemWriter, final ItemWriter<Customer> swaIntersectionItemWriter) {
        this.personRepo = personRepo;
        this.configuration = configuration;
        this.updateItemWriter = updateItemWriter;
        this.complementItemWriter = complementItemWriter;
        this.swwExceptionItemWriter = swwExceptionItemWriter;
        this.swaIntersectionItemWriter = swaIntersectionItemWriter;
    }

    @Override
    public ItemWriter<? super Customer> classify(Customer customer) {

        if (isEmpty(customer) || isFalse(customer.isValid())) {
            LOGGER.error("The SWW customer {} is missing a mandatory field", customer.toString());
            return swwExceptionItemWriter;
        }

        final List<Person> persons = personRepo.find(LdapQueryBuilder.query().where(DIGITAL_ID).is(customer.getUserID()).or(MAIL).is(customer.getName()));
        if (isEmpty(persons)) {
            LOGGER.info("The SWW customer {} will be created in the FR DS", customer.getName());
            return complementItemWriter;
        }

        if (persons.size() > 1) {
            LOGGER.warn("Multiple Entries Found. The SWA customer with the email {} or digitalId {} already exists in the FR DS", customer.getName(), customer.getUserID());
            return swaIntersectionItemWriter;
        }

        final Person person = persons.get(0);
        if (ObjectUtils.isEmpty(person.getCreatorsName()) || !person.getCreatorsName().equalsIgnoreCase(configuration.getLdap().getBindDN())) {
            LOGGER.warn("The SWA customer {} already exists in the FR DS", customer.getName());
            return swaIntersectionItemWriter;
        }


        if (isFalse(customer.getName().equalsIgnoreCase(person.getMail()))) {
            customer.setOldName(person.getMail());
        }
        LOGGER.info("The SWW customer {} already exists in the FR DS and the changes will be updated in the FR DS", customer.getName());
        return updateItemWriter;
    }
}
