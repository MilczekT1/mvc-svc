package pl.konradboniecki.budget.mvc.service;


import lombok.Getter;

/**
 * ErrorType provides constants for thymeleaf. Use this enum to avoid typos.
 **/

public enum ErrorType {

    INVALID_ACTIVATION_LINK("invalidActivationLink"),//TODO: ready to remove, extracted
    INVALID_INVITATION_LINK("invalidInvitationLink"),
    PROCESSING_EXCEPTION("processingException"),
    NOT_ENOUGH_SPACE_IN_FAMILY("notEnoughSpaceInFamily"),
    ALREADY_IN_FAMILY("alreadyInFamily");

    @Getter
    private String errorTypeVarName;

    ErrorType(String modelAttrName) {
        this.errorTypeVarName = modelAttrName;
    }
}
