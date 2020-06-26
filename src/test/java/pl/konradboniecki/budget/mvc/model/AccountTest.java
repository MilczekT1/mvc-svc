package pl.konradboniecki.budget.mvc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import pl.konradboniecki.budget.mvc.model.frontendforms.AccountForm;
import pl.konradboniecki.chassis.tools.HashGenerator;

import static org.junit.jupiter.api.Assertions.*;
import static pl.konradboniecki.budget.mvc.service.UserType.USER;

@TestInstance(Lifecycle.PER_CLASS)
public class AccountTest {

    @Test
    void testIfHasFamily() {
        Account acc = new Account();
        assertFalse(acc.hasFamily());
        acc.setFamilyId(1L);
        assertTrue(acc.hasFamily());
    }

    @Test
    void testHashPasswordInSetter() {
        String password = "P@ssw0rd";
        String hashedPassword = new HashGenerator().hashPassword(password);
        Account acc = new Account();
        acc.setPassword(password);
        assertEquals(acc.getPassword(), hashedPassword);
    }

    @Test
    void testLowerCaseEmail() {
        Account acc = new Account();
        acc.setEmail("TEST@MAIL.com");
        assertEquals("test@mail.com", acc.getEmail());
    }

    @Test
    void testInitFromAccountForm() {
        AccountForm accForm = new AccountForm();
        accForm.setPassword("password");
        accForm.setEmail("TEST@mail.com");
        accForm.setRepeatedPassword("password");
        accForm.setFirstName("kon");
        accForm.setLastName("bon");

        Account acc = new Account(accForm);
        String hashedPassword = new HashGenerator().hashPassword(accForm.getPassword());
        assertAll(
                () -> assertEquals(accForm.getEmail().toLowerCase(), acc.getEmail()),
                () -> assertEquals(accForm.getFirstName(), acc.getFirstName()),
                () -> assertEquals(hashedPassword, acc.getPassword()),
                () -> assertNotNull(acc.getRegisterDate()),
                () -> assertEquals(USER.getRoleName(), acc.getRole()),
                () -> assertFalse(acc.isEnabled()),
                () -> assertNull(acc.getId()),
                () -> assertNull(acc.getFamilyId()),
                () -> assertFalse(acc.isBudgetGranted()),
                () -> assertFalse(acc.isHorseeGranted())

        );
    }
}
