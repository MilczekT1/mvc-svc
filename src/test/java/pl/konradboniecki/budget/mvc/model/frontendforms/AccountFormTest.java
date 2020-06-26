package pl.konradboniecki.budget.mvc.model.frontendforms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(Lifecycle.PER_CLASS)
public class AccountFormTest {

    @Test
    void checkRepeatedPasswordTest() {
        AccountForm accountForm = new AccountForm();
        accountForm.setPassword("test_password");

        accountForm.setRepeatedPassword("test_password");
        assertTrue(accountForm.checkRepeatedPassword());

        accountForm.setRepeatedPassword("test_repeated_password");
        assertFalse(accountForm.checkRepeatedPassword());

    }
}
