package mssmfactory;

import mssmfactory.models.Field;
import mssmfactory.models.Model;
import mssmfactory.models.Module;
import mssmfactory.models.RelationshipField;
import mssmfactory.utils.OdooModelDesigner;
import mssmfactory.utils.OdooModelReader;
import mssmfactory.utils.OdooModelWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

public class MainClass {

    public static void main(String[] args) throws IOException {
        MainClass.odooSchemaLoading();
    }

    public static void odooSchemaLoading() throws IOException {
        Predicate<Module> modulePredicate = module -> module.getModuleId().startsWith("stock");

        OdooModelReader odooModelReader = new OdooModelReader("models.xlsx", "fields.xlsx");
        odooModelReader.process(modulePredicate, relationshipField ->
                relationshipField.getStored());

        OdooModelDesigner odooModelDesigner = new OdooModelDesigner(odooModelReader);
        odooModelDesigner.process();

        OdooModelWriter odooModelWriter = new OdooModelWriter(odooModelDesigner, "drawio.xml");
        odooModelWriter.process();
    }

    public static void customSchemaLoading() throws IOException {
        Module module = new Module();
        module.setModuleId("My Module Id");
        module.setModuleName("My Module Name");

        Map<String, Module> moduleMap = new HashMap<>();
        moduleMap.put("My Module Id", module);

        Model a = new Model();
        a.setModule(module);
        a.setModelId("A Model");
        a.setModelName("A Model");

        Model b = new Model();
        b.setModule(module);
        b.setModelId("B Model");
        b.setModelName("B Model");

        Field aField = new Field();
        aField.setFieldId("A id");
        aField.setModel(a);
        aField.setFieldName("A Identifiar");
        aField.setDatabaseFieldName("A_field");
        aField.setFieldType("Integer");
        a.getSimpleFields().add(aField);

        Field bField = new Field();
        bField.setFieldId("B id");
        bField.setModel(b);
        bField.setFieldName("B Identifiar");
        bField.setDatabaseFieldName("B_field");
        bField.setFieldType("Integer");
        b.getSimpleFields().add(bField);

        RelationshipField aRelationshipField = new RelationshipField();
        aRelationshipField.setRelationshipFieldType(RelationshipField.RelationshipFieldType.MANY2ONE);
        aRelationshipField.setTarget(b);
        aRelationshipField.setFieldId("AB id");
        aRelationshipField.setModel(a);
        aRelationshipField.setFieldName("AB Identifiar");
        aRelationshipField.setDatabaseFieldName("AB_field");
        aRelationshipField.setFieldType("one2many");

        RelationshipField bRelationshipField = new RelationshipField();
        bRelationshipField.setRelationshipFieldType(RelationshipField.RelationshipFieldType.MANY2MANY);
        bRelationshipField.setTarget(a);
        bRelationshipField.setFieldId("BA id");
        bRelationshipField.setModel(b);
        bRelationshipField.setFieldName("BA Identifiar");
        bRelationshipField.setDatabaseFieldName("BA_field");
        bRelationshipField.setFieldType("many2one");

        a.getComplexFields().add(aRelationshipField);
        b.getComplexFields().add(bRelationshipField);

        Map<String, Model> modelMap = new HashMap<>();
        modelMap.put("A Model", a);
        modelMap.put("B Model", b);

        OdooModelReader odooModelReader = new OdooModelReader("models.xlsx", "fields.xlsx");
        odooModelReader.setModuleMap(moduleMap);
        odooModelReader.setModelMap(modelMap);

        OdooModelDesigner odooModelDesigner = new OdooModelDesigner(odooModelReader);
        odooModelDesigner.process();

        OdooModelWriter odooModelWriter = new OdooModelWriter(odooModelDesigner, "drawio.xml");
        odooModelWriter.process();
    }
}
