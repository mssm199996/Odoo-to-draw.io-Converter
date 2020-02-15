package mssmfactory.utils;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.Setter;
import mssmfactory.models.Field;
import mssmfactory.models.Model;
import mssmfactory.models.Module;
import mssmfactory.models.RelationshipField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Getter
@Setter
public class OdooModelDesigner {

    public static int GAP_BETWEEN_MODELS = 600;

    private OdooModelReader odooModelReader;

    public OdooModelDesigner(OdooModelReader odooModelReader) {
        this.odooModelReader = odooModelReader;
    }

    public void process() {
        this.processModelsDimensions();
        this.processModulesColorsAndDimensionsAndIntervals();
        this.processModelsPositions();
        this.processFieldsPositions();
    }

    private void processModelsDimensions() {
        this.odooModelReader.getModelMap().forEach((modelId, model) -> {
            model.setWidth(this.getModelWidth(model));
            model.setHeight(this.getModelHeight(model));
        });
    }

    private void processModulesColorsAndDimensionsAndIntervals() {
        int numberOfModulesPerLine = (int) Math.ceil(Math.sqrt(this.odooModelReader.getModuleMap().size()));

        int maxY1PerLine = OdooModelWriter.WRITABLE_AREA_Y0;
        int x0 = OdooModelWriter.WRITABLE_AREA_X0;
        int y0 = OdooModelWriter.WRITABLE_AREA_Y0;
        int i = 0;

        for (Module module : this.odooModelReader.getModuleMap().values()) {
            int moduleAreaFactor = (int) Math.ceil(Math.sqrt(module.getModels().size() / 4));

            int moduleWidth = Math.min(this.getModuleWidth(module), moduleAreaFactor * OdooModelWriter.PAGE_WIDTH);
            int moduleHeight = Math.min(this.getModuleHeight(module), moduleAreaFactor * OdooModelWriter.PAGE_HEIGHT);

            module.setFillColor(this.getRandomFillColor());
            module.setFontColor(this.getFontColor(module.getFillColor()));

            module.setX0(x0);
            module.setY0(y0);
            module.setWidth(moduleWidth);
            module.setHeight(moduleHeight);

            module.setX1(x0 + moduleWidth);
            module.setY1(y0 + moduleHeight);

            maxY1PerLine = Math.max(maxY1PerLine, module.getY1());

            if (++i == numberOfModulesPerLine) {
                x0 = OdooModelWriter.WRITABLE_AREA_X0;
                y0 = maxY1PerLine + 2 * OdooModelWriter.GAP;

                i = 0;
            } else {
                x0 = module.getX1() + 2 * OdooModelWriter.GAP;
            }
        }
    }

    private void processModelsPositions() {
        BiFunction<Model, Model, Point2D>[] positionsCalculators = new BiFunction[8];
        positionsCalculators[0] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() + oldModel.getWidth() + OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY());

        positionsCalculators[1] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() + oldModel.getWidth() + OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY() - newModel.getHeight() - OdooModelDesigner.GAP_BETWEEN_MODELS);

        positionsCalculators[2] = (oldModel, newModel) -> new Point2D(
                oldModel.getX(),
                oldModel.getY() - newModel.getHeight() - OdooModelDesigner.GAP_BETWEEN_MODELS);

        positionsCalculators[3] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() - newModel.getWidth() - OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY() - newModel.getHeight() - OdooModelDesigner.GAP_BETWEEN_MODELS);

        positionsCalculators[4] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() - newModel.getWidth() - OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY());

        positionsCalculators[5] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() - newModel.getWidth() - OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY() + oldModel.getHeight() + OdooModelDesigner.GAP_BETWEEN_MODELS);

        positionsCalculators[6] = (oldModel, newModel) -> new Point2D(
                oldModel.getX(),
                oldModel.getY() + oldModel.getHeight() + OdooModelDesigner.GAP_BETWEEN_MODELS);

        positionsCalculators[7] = (oldModel, newModel) -> new Point2D(
                oldModel.getX() + oldModel.getWidth() + OdooModelDesigner.GAP_BETWEEN_MODELS,
                oldModel.getY() + oldModel.getHeight() + OdooModelDesigner.GAP_BETWEEN_MODELS);


        for (Module module : this.odooModelReader.getModuleMap().values()) {
            List<Model> models = module.getModels()
                    .stream()
                    .sorted((model1, model2) -> model2.getNumberOfComplexFields().compareTo(model1.getNumberOfComplexFields()))
                    .collect(Collectors.toList());

            if (!models.isEmpty()) {
                final int numberOfDirections = positionsCalculators.length;

                Model middleModel = models.get(0);
                ArrayList<Model> previousModels = new ArrayList<>(Collections.nCopies(
                        numberOfDirections, middleModel));

                middleModel.setX(module.getX0() + module.getWidth() / 2);
                middleModel.setY(module.getY0() + module.getHeight() / 2);

                for (int i = 1; i < models.size(); i++) {
                    int directionIndex = (i - 1) % numberOfDirections;

                    Model oldModel = previousModels.get(directionIndex);
                    Model newModel = models.get(i);

                    Point2D position = positionsCalculators[directionIndex].apply(oldModel, newModel);

                    newModel.setX((int) position.getX());
                    newModel.setY((int) position.getY());

                    previousModels.set(directionIndex, newModel);
                }
            }
        }
    }

    private void processFieldsPositions() {
        for (Model model : this.odooModelReader.getModelMap().values()) {
            for (RelationshipField relationshipField : model.getComplexFields()) {
                double exitX;
                double entryX;

                Model source = model;
                Model target = relationshipField.getTarget();

                Double exitYPositon = Math.random();
                Double entryYPositon = Math.random();

                if (source.equals(target)) {
                    exitX = 1;
                    entryX = 1;
                } else {
                    if (source.getX() < target.getX()) {
                        exitX = 1;
                        entryX = 0;
                    } else {
                        exitX = 0;
                        entryX = 1;
                    }
                }

                relationshipField.setEntryX(entryX);
                relationshipField.setExitX(exitX);
                relationshipField.setEntryY(entryYPositon);
                relationshipField.setExitY(exitYPositon);
                relationshipField.setColor(this.getRandomRelationShipColor());

                this.updateRelationshipFieldDrawable(relationshipField);
            }
        }
    }

    private void updateRelationshipFieldDrawable(RelationshipField relationshipField) {
        if (relationshipField.getDrawable() == null) {
            Model target = relationshipField.getTarget();

            List<RelationshipField> complexFields = target.getComplexFields()
                    .stream()
                    .filter(homologueRelationShip -> relationshipField.isHomologue(homologueRelationShip))
                    .collect(Collectors.toList());

            if (complexFields.isEmpty())
                relationshipField.setDrawable(true);
            else {
                RelationshipField homologueRelationShip = complexFields.get(0);
                homologueRelationShip.setDrawable(false);

                relationshipField.setDrawable(true);
            }
        }
    }

    private Color getRandomRelationShipColor() {
        return Color.hsb((new Random()).nextInt(360), 1.0, 0.5);
    }

    private int getModuleWidth(Module module) {
        return module.getModels().stream().mapToInt(Model::getWidth).sum();
    }

    private int getModuleHeight(Module module) {
        return module.getModels().stream().mapToInt(Model::getHeight).sum();
    }

    private int getModelHeight(Model model) {
        return OdooModelWriter.DEFAULT_FIELD_HEIGHT * model.getNumberOfSimpleFields() +
                OdooModelWriter.DEFAULT_HEADER_HEIGHT;
    }

    private int getModelWidth(Model model) {
        Module module = model.getModule();

        int max = Math.max(model.getModelName().length(), module.getModuleName().length()) * OdooModelWriter.HEADER_CHARACTER_WIDTH
                + OdooModelWriter.HEADER_CHARACTER_WITH_GAP;

        for (Field field : model.getSimpleFields())
            max = Math.max(max, field.getFieldName().length() * OdooModelWriter.FIELDS_CHARACTER_WIDTH +
                    +OdooModelWriter.FIELDS_CHARACTER_WIDTH_GAP);

        return max;
    }

    private Color getRandomFillColor() {
        return Color.hsb((new Random()).nextInt(360), 1.0, 1.0);
    }

    private Color getFontColor(Color fillColor) {
        Color result = fillColor.invert().grayscale();

        if (result.getBrightness() >= 0.5)
            return Color.WHITE;

        return Color.BLACK;
    }
}
