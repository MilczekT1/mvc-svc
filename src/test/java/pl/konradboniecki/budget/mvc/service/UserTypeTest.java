package pl.konradboniecki.budget.mvc.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

@TestInstance(Lifecycle.PER_CLASS)
public class UserTypeTest {

    @Test
    public void testDescription() {

        Assertions.assertAll(
                () -> Assertions.assertEquals("USER", UserType.USER.getRoleName()),
                () -> Assertions.assertEquals("SUBSCRIBER", UserType.SUBSCRIBER.getRoleName()),
                () -> Assertions.assertEquals("ADMIN", UserType.ADMIN.getRoleName())
        );
    }
}
