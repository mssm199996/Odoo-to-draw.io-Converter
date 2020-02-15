package mssmfactory.utils;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mssmfactory.models.Field;
import mssmfactory.models.Model;
import mssmfactory.models.Module;
import mssmfactory.models.RelationshipField;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

@Getter
@Setter
@NoArgsConstructor
public class OdooModelReader {

    private String modelsFilePath, fieldsFilePath;

    private Map<String, Module> moduleMap;
    private Map<String, Model> modelMap;
    private Map<String, Model> modelNameToModelMap;

    public OdooModelReader(String modelsFilePath, String fieldsFilePath) {
        this.modelsFilePath = modelsFilePath;
        this.fieldsFilePath = fieldsFilePath;
    }

    public void process(Predicate<Module> modulePredicate, Predicate<Field> fieldPredicate) throws IOException {
        this.processModuleAndModelsCollection(modulePredicate);
        this.processFieldsCollection(modulePredicate, fieldPredicate);
    }

    public void processModuleAndModelsCollection(Predicate<Module> modulePredicate) throws IOException {
        this.moduleMap = new LinkedHashMap<>();
        this.modelMap = new LinkedHashMap<>();
        this.modelNameToModelMap = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(new File(this.modelsFilePath))) {
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            sheet.forEach(row -> {
                if (row.getRowNum() != 0) {
                    try {
                        String modelId = row.getCell(0).getStringCellValue();
                        String modelName = row.getCell(1).getStringCellValue();
                        Boolean modelTransient = row.getCell(3).getBooleanCellValue();

                        Model model = new Model();
                        model.setModelId(modelId);
                        model.setModelName(modelName);
                        model.setModelTransient(modelTransient);

                        String moduleId = modelId.split("\\.")[0];
                        Module module = this.moduleMap.get(moduleId);

                        boolean newModule = false;

                        if (module == null) {
                            module = new Module();
                            module.setModuleId(moduleId);
                            module.setModuleName(moduleId.toUpperCase());

                            newModule = true;
                        }

                        if (modulePredicate == null || modulePredicate.test(module)) {
                            if (newModule)
                                this.moduleMap.put(moduleId, module);

                            model.setModule(module);
                            module.getModels().add(model);

                            this.modelMap.put(modelId, model);
                            this.modelNameToModelMap.put(modelName, model);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    public void processFieldsCollection(Predicate<Module> modulePredicate, Predicate<Field> fieldPredicate) throws IOException {
        try (FileInputStream fis = new FileInputStream(new File(this.fieldsFilePath))) {
            XSSFWorkbook workbook = new XSSFWorkbook(fis);
            XSSFSheet sheet = workbook.getSheetAt(0);

            sheet.forEach(row -> {
                if (row.getRowNum() != 0) {
                    try {
                        String databaseFieldName = row.getCell(0).getStringCellValue();
                        String fieldName = row.getCell(1).getStringCellValue();
                        String modelName = row.getCell(2).getStringCellValue();
                        String fieldType = row.getCell(3).getStringCellValue();

                        Boolean indexed = row.getCell(5).getBooleanCellValue();
                        Boolean stored = row.getCell(6).getBooleanCellValue();
                        Boolean readOnly = row.getCell(7).getBooleanCellValue();

                        Model model = this.modelNameToModelMap.get(modelName);
                        Field field = null;

                        if (model != null && (modulePredicate == null || modulePredicate.test(model.getModule()))) {
                            boolean isRelationshipField = false;

                            if (fieldType.equals("one2many") || fieldType.equals("many2one") || fieldType.equals("many2many")) {
                                String targetModelId = row.getCell(8).getStringCellValue();
                                Model target = this.modelMap.get(targetModelId);

                                if (target != null) {
                                    RelationshipField relationshipField = new RelationshipField();
                                    relationshipField.setTarget(target);
                                    relationshipField.setRelationshipFieldType(
                                            RelationshipField.RelationshipFieldType.valueOf(fieldType.toUpperCase()));

                                    field = relationshipField;


                                    isRelationshipField = true;
                                }
                            } else
                                field = new Field();

                            if (field != null) {
                                String fieldId = model.getModelId() + "_" + field.getFieldName() + "_" + Field.INDEX++;

                                field.setFieldId(fieldId);
                                field.setDatabaseFieldName(databaseFieldName);
                                field.setFieldName(fieldName);
                                field.setFieldType(fieldType);
                                field.setIndexed(indexed);
                                field.setModel(model);
                                field.setReadOnly(readOnly);
                                field.setStored(stored);

                                if (fieldPredicate == null || fieldPredicate.test(field)) {
                                    if (isRelationshipField) {
                                        model.getComplexFields().add((RelationshipField) field);
                                        model.setNumberOfComplexFields(model.getNumberOfComplexFields() + 1);
                                    } else {
                                        model.getSimpleFields().add(field);
                                        model.setNumberOfSimpleFields(model.getNumberOfSimpleFields() + 1);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
