package com.sw.repository;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Person {

    private String uid;

    private String mail;

    private String digitalId;

    private String fullName;

    private String lastName;

    private String inetuserstatus;

    private String creatorsName;

    public boolean equals(Object obj) {

        if (!(obj instanceof Person)) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Person person = (Person) obj;

        return person.uid.equals(this.uid);
    }

    public int hashCode() {
        return uid.hashCode();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(
                this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
