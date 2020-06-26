package pl.konradboniecki.budget.mvc.service.client;

import lombok.Getter;

public enum FamilyIdType {

    FAMILY_ID("id"), OWNER_ID("ownerId");

    @Getter
    private String queryParamName;

    FamilyIdType(String queryParamName) {
        this.queryParamName = queryParamName;
    }
}
