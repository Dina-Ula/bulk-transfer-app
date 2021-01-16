package com.sw.repository;

import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.AbstractContextMapper;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.SearchScope;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.stereotype.Component;

import javax.naming.Name;
import java.util.List;

import static com.sw.repository.Constants.Person.Attribute.CN;
import static com.sw.repository.Constants.Person.Attribute.CREATORS_NAME;
import static com.sw.repository.Constants.Person.Attribute.DIGITAL_ID;
import static com.sw.repository.Constants.Person.Attribute.INET_USER_STATUS;
import static com.sw.repository.Constants.Person.Attribute.MAIL;
import static com.sw.repository.Constants.Person.Attribute.SN;
import static com.sw.repository.Constants.Person.Attribute.UID;
import static com.sw.repository.Constants.Person.ObjectClass.CUSTOM_OBJECT_CLASS;
import static com.sw.repository.Constants.Person.ObjectClass.INET_ORG_PERSON;
import static com.sw.repository.Constants.Person.ObjectClass.INET_USER;
import static com.sw.repository.Constants.Person.ObjectClass.OBJECT_CLASS;
import static com.sw.repository.Constants.Person.ObjectClass.ORGANIZATIONAL_PERSON;
import static com.sw.repository.Constants.Person.ObjectClass.PERSON;
import static com.sw.repository.Constants.Person.ObjectClass.TOP;
import static org.springframework.ldap.query.LdapQueryBuilder.query;

@Component
public class PersonRepoImpl implements PersonRepo {

    private final static String[] ALL_ATTRIBUTES = new String[]{UID, MAIL, DIGITAL_ID, CN, SN, INET_USER_STATUS, CREATORS_NAME};

    private final LdapTemplate ldapTemplate;


    public PersonRepoImpl(final LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public void create(Person person) {
        DirContextAdapter context = new DirContextAdapter(buildDn(person));
        mapToContext(person, context);
        ldapTemplate.bind(context);
    }

    public void update(Person person) {
        Name dn = buildDn(person);
        DirContextOperations context = ldapTemplate.lookupContext(dn);
        mapToContext(person, context);
        ldapTemplate.modifyAttributes(context);
    }

    public void delete(Person person) {
        ldapTemplate.unbind(buildDn(person));
    }

    public void rename(String oldDn, String newDn) {
        ldapTemplate.rename(buildDn(oldDn), buildDn(newDn));
    }

    @Override
    public List<Person> find(LdapQuery query) {
        return ldapTemplate.search(query.base(), query.filter().encode(), SearchScope.SUBTREE.getId(), ALL_ATTRIBUTES, getContextMapper());
    }

    public Person findByUid(String uid) {
        Name dn = buildDn(uid);
        return ldapTemplate.lookup(dn, getContextMapper());
    }

    public List<Person> findAll() {
        EqualsFilter filter = new EqualsFilter(OBJECT_CLASS, PERSON);
        return ldapTemplate.search(LdapUtils.emptyLdapName(), filter.encode(), getContextMapper());
    }

    public List<Person> findByMail(String mail) {
        LdapQuery query = query().where(MAIL).is(mail);
        return ldapTemplate.search(query.base(), query.filter().encode(), SearchScope.SUBTREE.getId(), ALL_ATTRIBUTES, getContextMapper());
    }

    @Override
    public List<Person> findByDigitalId(String digitalId) {
        LdapQuery query = query().where(DIGITAL_ID).is(digitalId);
        return ldapTemplate.search(query.base(), query.filter().encode(), SearchScope.SUBTREE.getId(), ALL_ATTRIBUTES, getContextMapper());
    }

    public List<String> getAllUid() {
        return ldapTemplate.search(
                query().where(OBJECT_CLASS).is(PERSON),
                (AttributesMapper<String>) attrs -> attrs.get(UID).get().toString());
    }

    protected ContextMapper<Person> getContextMapper() {
        return new PersonContextMapper();
    }

    protected Name buildDn(Person person) {
        return buildDn(person.getUid());
    }

    protected Name buildDn(String uid) {
        return LdapNameBuilder.newInstance()
                .add(UID, uid)
                .build();
    }

    protected void mapToContext(Person person, DirContextOperations context) {
        context.setAttributeValues(OBJECT_CLASS, new String[]{TOP, PERSON, ORGANIZATIONAL_PERSON, INET_ORG_PERSON, INET_USER, CUSTOM_OBJECT_CLASS});
        context.setAttributeValue(MAIL, person.getMail());
        context.setAttributeValue(CN, person.getFullName());
        context.setAttributeValue(SN, person.getLastName());
        context.setAttributeValue(DIGITAL_ID, person.getDigitalId());
        context.setAttributeValue(INET_USER_STATUS, person.getInetuserstatus());
    }

    private static class PersonContextMapper extends AbstractContextMapper<Person> {
        public Person doMapFromContext(DirContextOperations context) {
            Person person = new Person();
            person.setUid(context.getStringAttribute(UID));
            person.setMail(context.getStringAttribute(MAIL));
            person.setFullName(context.getStringAttribute(CN));
            person.setLastName(context.getStringAttribute(SN));
            person.setDigitalId(context.getStringAttribute(DIGITAL_ID));
            person.setCreatorsName(context.getStringAttribute(CREATORS_NAME));
            person.setInetuserstatus(context.getStringAttribute(INET_USER_STATUS));
            return person;
        }
    }
}
