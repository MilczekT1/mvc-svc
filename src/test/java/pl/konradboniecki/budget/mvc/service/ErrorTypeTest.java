package pl.konradboniecki.budget.mvc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class ErrorTypeTest {

    @Test
    public void testDescription() {

        Assertions.assertAll(
                () -> Assertions.assertEquals("alreadyInFamily", ErrorType.ALREADY_IN_FAMILY.getErrorTypeVarName()),
                () -> Assertions.assertEquals("invalidInvitationLink", ErrorType.INVALID_INVITATION_LINK.getErrorTypeVarName()),
                () -> Assertions.assertEquals("invalidActivationLink", ErrorType.INVALID_ACTIVATION_LINK.getErrorTypeVarName()),
                () -> Assertions.assertEquals("processingException", ErrorType.PROCESSING_EXCEPTION.getErrorTypeVarName()),
                () -> Assertions.assertEquals("notEnoughSpaceInFamily", ErrorType.NOT_ENOUGH_SPACE_IN_FAMILY.getErrorTypeVarName())
        );
    }
}
