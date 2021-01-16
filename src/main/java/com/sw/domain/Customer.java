package com.sw.domain;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Getter
@Setter
public class Customer {

    private final String name;

    private final String userID;

    private final String displayName;

    private String oldName;

    public Customer(String name, String userID, String displayName) {
        this.name = name;
        this.userID = userID;
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(
                this, ToStringStyle.NO_CLASS_NAME_STYLE);
    }

    public boolean isValid() {
        return ObjectUtils.isNotEmpty(this.name) && ObjectUtils.isNotEmpty(this.userID) && ObjectUtils.isNotEmpty(this.displayName);
    }
}
