package pl.konradboniecki.budget.mvc.model;

import lombok.Data;
import lombok.experimental.Accessors;
import pl.konradboniecki.budget.mvc.model.frontendforms.JarCreationForm;

@Data
@Accessors(chain = true)
public class Jar {

    private Long id;
    private Long budgetId;
    private String jarName;
    private Long currentAmount;
    private Long capacity;
    private String status;

    public Jar() {
        setCapacity(0L);
        setCurrentAmount(0L);
        setStatus("IN PROGRESS");
    }

    public Jar(JarCreationForm jarCreationForm) {
        setJarName(jarCreationForm.getJarName());
        setCapacity(jarCreationForm.getCapacity());
        setCurrentAmount(0L);
        setStatus("IN PROGRESS");
    }
}