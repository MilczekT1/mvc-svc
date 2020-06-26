package pl.konradboniecki.budget.mvc.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

public class ViewTemplateTest {

    @Test
    void viewTemplateTest() {
        try {
            Constructor constructor = ViewTemplate.class.getDeclaredConstructor();
            constructor.setAccessible(true);

            Object viewTemplate = constructor.newInstance();
            assertTrue(viewTemplate instanceof ViewTemplate);

            Field[] fields = ViewTemplate.class.getFields();
            for (Field field : fields) {
                assertNotNull(field);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
