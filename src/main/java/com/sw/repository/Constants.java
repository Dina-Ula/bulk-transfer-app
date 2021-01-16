package com.sw.repository;

public class Constants {

    public static final class Person {
        public static final class ObjectClass {
            public static final String OBJECT_CLASS = "objectclass";
            public static final String TOP = "top";
            public static final String PERSON = "person";
            public static final String ORGANIZATIONAL_PERSON = "organizationalPerson";
            public static final String INET_ORG_PERSON = "inetOrgPerson";
            public static final String INET_USER = "inetuser";
            public static final String CUSTOM_OBJECT_CLASS = "customObjectclass";
        }

        public static final class Attribute {
            public static final String CN = "cn";
            public static final String SN = "sn";
            public static final String UID = "uid";
            public static final String MAIL = "mail";
            public static final String DIGITAL_ID = "digitalId";
            public static final String CREATORS_NAME = "creatorsName";
            public static final String INET_USER_STATUS = "inetuserstatus";
        }
    }
}
