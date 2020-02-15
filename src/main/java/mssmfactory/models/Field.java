package mssmfactory.models;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Field {

    public static int INDEX = 0;

    private String fieldId;
    private String databaseFieldName;
    private String fieldName;
    private String fieldType;
    private Model model;

    private Boolean indexed;
    private Boolean stored;
    private Boolean readOnly;
}
