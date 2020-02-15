package mssmfactory.models;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@ToString(exclude = {"simpleFields", "complexFields"})
public class Model {

    private String modelId;
    private String modelName;
    private boolean modelTransient;

    private Module module;

    private Set<Field> simpleFields = new LinkedHashSet<>();
    private Set<RelationshipField> complexFields = new LinkedHashSet<>();

    private Integer numberOfSimpleFields = 0, numberOfComplexFields = 0, x = 0, y = 0, width = 0, height = 0;

    @Override
    public boolean equals(Object o) {
        return o != null && ((this == o) || (o instanceof Model && this.getModelId().equals((((Model) o).getModelId()))));
    }
}
