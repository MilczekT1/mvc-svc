package pl.konradboniecki.budget.mvc.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import pl.konradboniecki.budget.mvc.model.frontendforms.JarCreationForm;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
public class JarTest {

    @Test
    public void constructorTest() {
        String jarName = "testName";
        Long capacity = 15L;

        JarCreationForm jarCreationForm = new JarCreationForm();
        jarCreationForm.setJarName(jarName);
        jarCreationForm.setCapacity(capacity);

        Jar jar = new Jar(jarCreationForm);

        assertAll(
                () -> assertEquals(jarName, jar.getJarName()),
                () -> assertEquals(capacity, jar.getCapacity()),
                () -> assertEquals(0L, jar.getCurrentAmount().longValue()),
                () -> assertNull(jar.getBudgetId()),
                () -> assertNull(jar.getId())
        );
    }

}
