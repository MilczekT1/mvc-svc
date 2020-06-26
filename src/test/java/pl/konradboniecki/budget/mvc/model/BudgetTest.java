package pl.konradboniecki.budget.mvc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class BudgetTest {

    @Test
    public void testContructor() {
        Budget budget = new Budget();
        assertAll(
                () -> assertEquals(6L, budget.getMaxJars().longValue()),
                () -> assertNull(budget.getId()),
                () -> assertNull(budget.getFamilyId())
        );
    }
}
