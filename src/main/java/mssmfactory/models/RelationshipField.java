package mssmfactory.models;

import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.lang.annotation.Target;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
public class RelationshipField extends Field {

    private Model target;
    private RelationshipFieldType relationshipFieldType;

    private double entryX, entryY, exitX, exitY;
    private Color color;

    private Boolean drawable = null;

    public boolean isHomologue(RelationshipField relationshipField) {
        Model source = this.getModel();
        Model target = this.getTarget();

        Model homologueSource = relationshipField.getModel();
        Model homologueTarget = relationshipField.getTarget();

        boolean homologueModels = source.equals(homologueTarget) && target.equals(homologueSource);

        if (!homologueModels)
            return false;

        if (this.getRelationshipFieldType() == RelationshipFieldType.ONE2MANY && relationshipField.getRelationshipFieldType() == RelationshipFieldType.MANY2ONE)
            return true;
        else if (this.getRelationshipFieldType() == RelationshipFieldType.MANY2ONE && relationshipField.getRelationshipFieldType() == RelationshipFieldType.ONE2MANY)
            return true;
        else if (this.getRelationshipFieldType() == RelationshipFieldType.MANY2MANY && relationshipField.getRelationshipFieldType() == RelationshipFieldType.MANY2MANY)
            return true;

        return false;
    }

    public enum RelationshipFieldType {
        ONE2MANY,
        MANY2ONE,
        MANY2MANY;
    }
}
