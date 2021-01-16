package com.sw.repository;

import org.springframework.ldap.query.LdapQuery;

import java.util.List;

public interface PersonRepo {
    void create(Person person);

    void update(Person person);

    void delete(Person person);

    void rename(String oldDn, String newDn);

    List<Person> find(LdapQuery query);

    List<Person> findAll();

    Person findByUid(String uid);

    List<Person> findByMail(String mail);

    List<Person> findByDigitalId(String digitalId);

    List<String> getAllUid();
}
