package mssmfactory.models;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = "models")
public class Module {
    private String moduleId;
    private String moduleName;
    private Color fillColor, fontColor;

    private int x0, y0, x1, y1, width, height;
    private Set<Model> models = new LinkedHashSet<>();
}
