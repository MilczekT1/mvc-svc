package pl.konradboniecki.budget.mvc.service;

import lombok.Getter;

/**
 * UserType provides user roles and properties.
 **/

public enum UserType {

    ADMIN("ADMIN"),
    SUBSCRIBER("SUBSCRIBER"),
    USER("USER");

    @Getter
    private String roleName;

    UserType(String roleName) {
        this.roleName = roleName;
    }
}
