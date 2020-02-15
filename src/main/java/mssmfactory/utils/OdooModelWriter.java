package mssmfactory.utils;

import javafx.scene.paint.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mssmfactory.models.Field;
import mssmfactory.models.Model;
import mssmfactory.models.Module;
import mssmfactory.models.RelationshipField;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Map;
import java.util.function.Predicate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OdooModelWriter {
    public static final int FIELDS_CHARACTER_WIDTH = 7;
    public static final int FIELDS_CHARACTER_WIDTH_GAP = 14;

    // --------------------------------------------------------------------------------------------

    public static int HEADER_CHARACTER_WIDTH = 10;
    public static int HEADER_CHARACTER_WITH_GAP = 4;

    // --------------------------------------------------------------------------------------------

    public static int PAGE_WIDTH = 827;
    public static int PAGE_HEIGHT = 1169;

    public static int GAP = 25;

    public static int WRITABLE_PAGE_WIDTH = OdooModelWriter.PAGE_WIDTH - 2 * OdooModelWriter.GAP;
    public static int WRITABLE_PAGE_HEIGHT = OdooModelWriter.PAGE_HEIGHT - 2 * OdooModelWriter.GAP;

    public static int WRITABLE_AREA_X0 = OdooModelWriter.GAP;
    public static int WRITABLE_AREA_Y0 = OdooModelWriter.GAP;

    public static int WRITABLE_AREA_X1 = OdooModelWriter.PAGE_WIDTH - OdooModelWriter.GAP;
    public static int WRITABLE_AREA_Y1 = OdooModelWriter.PAGE_HEIGHT - OdooModelWriter.GAP;

    // --------------------------------------------------------------------------------------------

    public static int FIELDS_STARTING_Y_POSITION = 46;
    public static int FIELDS_Y_POSITION_STEP = 26;

    public static int DEFAULT_HEADER_HEIGHT = 46;
    public static int DEFAULT_FIELD_HEIGHT = 26;

    // --------------------------------------------------------------------------------------------

    private static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder DOCUMENT_BUILDER;

    private static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    private static Transformer TRANSFORMER;

    static {
        try {
            OdooModelWriter.DOCUMENT_BUILDER = OdooModelWriter.DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();

            OdooModelWriter.TRANSFORMER = OdooModelWriter.TRANSFORMER_FACTORY.newTransformer();
            OdooModelWriter.TRANSFORMER.setOutputProperty(OutputKeys.INDENT, "yes");
            OdooModelWriter.TRANSFORMER.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        } catch (ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        }
    }

    private OdooModelDesigner odooModelDesigner;
    private String filePath;

    public void process() {
        try {
            Document document = OdooModelWriter.DOCUMENT_BUILDER.newDocument();

            Element mxGraphModel = this.constructRootElement(document);
            Element root = this.constructWorkableRootElement(document, mxGraphModel);

            document.appendChild(mxGraphModel);

            odooModelDesigner.getOdooModelReader().getModelMap().forEach((modelId, model) -> {
                this.constructModelElement(document, root, model);
            });

            DOMSource domSource = new DOMSource(document);
            StreamResult streamResult = new StreamResult(new File(this.filePath));

            OdooModelWriter.TRANSFORMER.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public Element constructWorkableRootElement(Document document, Element mxGraphModel) {
        Element mxCell0 = document.createElement("mxCell");
        mxCell0.setAttribute("id", "0");

        Element mxCell1 = document.createElement("mxCell");
        mxCell1.setAttribute("id", "1");
        mxCell1.setAttribute("parent", "0");

        Element root = document.createElement("root");
        root.appendChild(mxCell0);
        root.appendChild(mxCell1);

        mxGraphModel.appendChild(root);

        return root;
    }

    public Element constructRootElement(Document document) {
        Element mxGraphModel = document.createElement("mxGraphModel");
        mxGraphModel.setAttribute("dx", "4956");
        mxGraphModel.setAttribute("dy", "2301");
        mxGraphModel.setAttribute("grid", "1");
        mxGraphModel.setAttribute("gridSize", "10");
        mxGraphModel.setAttribute("guides", "1");
        mxGraphModel.setAttribute("tooltips", "1");
        mxGraphModel.setAttribute("connect", "1");
        mxGraphModel.setAttribute("arrows", "1");
        mxGraphModel.setAttribute("fold", "1");
        mxGraphModel.setAttribute("page", "1");
        mxGraphModel.setAttribute("pageScale", "1");
        mxGraphModel.setAttribute("pageWidth", Integer.toString(OdooModelWriter.PAGE_WIDTH));
        mxGraphModel.setAttribute("pageHeight", Integer.toString(OdooModelWriter.PAGE_HEIGHT));
        mxGraphModel.setAttribute("shadow", "0");

        return mxGraphModel;
    }

    public void constructModelElement(Document document, Element root, Model model) {
        Module module = model.getModule();

        String fillColor = this.getColorAsHex(model.getModule().getFillColor());
        String fontColor = this.getColorAsHex(model.getModule().getFontColor());

        String modelTitle = "<" + module.getModuleName() + ">\n" + model.getModelName();

        Element modelElement = document.createElement("mxCell");
        modelElement.setAttribute("id", model.getModelId());
        modelElement.setAttribute("value", modelTitle);
        modelElement.setAttribute("vertex", "1");
        modelElement.setAttribute("parent", "1");
        modelElement.setAttribute("style", "swimlane;" +
                "fontStyle=1;" +
                "childLayout=stackLayout;" +
                "horizontal=1;" +
                "startSize=46;" +
                "fillColor=" + fillColor + ";" +
                "horizontalStack=0;" +
                "resizeParent=1;" +
                "resizeParentMax=0;" +
                "resizeLast=0;" +
                "collapsible=1;" +
                "marginBottom=0;" +
                "fontColor=" + fontColor + ";" +
                "strokeColor=#000000;" +
                "fontSize=14;");

        Element modelPosition = document.createElement("mxGeometry");
        modelPosition.setAttribute("x", String.valueOf(model.getX()));
        modelPosition.setAttribute("y", String.valueOf(model.getY()));
        modelPosition.setAttribute("width", String.valueOf(model.getWidth()));
        modelPosition.setAttribute("as", "geometry");
        modelPosition.setAttribute("height", String.valueOf(model.getHeight()));

        modelElement.appendChild(modelPosition);

        root.appendChild(modelElement);

        this.constructFieldElements(document, root, model, model.getWidth());
        this.constructComplexFieldElements(document, root, model);
    }

    private void constructFieldElements(Document document, Element root, Model model, int modelWidth) {
        int currentYPosition = OdooModelWriter.FIELDS_STARTING_Y_POSITION;

        for (Field field : model.getSimpleFields()) {
            String fieldValue = "+ " + field.getFieldName() + ": " + field.getFieldType();

            Element fieldElement = document.createElement("mxCell");
            fieldElement.setAttribute("id", field.getFieldId());
            fieldElement.setAttribute("value", fieldValue);
            fieldElement.setAttribute("parent", model.getModelId());
            fieldElement.setAttribute("vertex", "1");
            fieldElement.setAttribute("style", "text;" +
                    "strokeColor=none;" +
                    "fillColor=none;" +
                    "align=left;" +
                    "verticalAlign=top;" +
                    "spacingLeft=4;" +
                    "spacingRight=4;" +
                    "overflow=hidden;" +
                    "rotatable=0;" +
                    "points=[[0,0.5],[1,0.5]];" +
                    "portConstraint=eastwest;" +
                    "fontSize=12;" +
                    "fontStyle=1");

            Element fieldPositionElement = document.createElement("mxGeometry");
            fieldPositionElement.setAttribute("y", String.valueOf(currentYPosition));
            fieldPositionElement.setAttribute("height", "26");
            fieldPositionElement.setAttribute("as", "geometry");
            fieldPositionElement.setAttribute("width", String.valueOf(modelWidth));

            fieldElement.appendChild(fieldPositionElement);

            root.appendChild(fieldElement);

            currentYPosition += OdooModelWriter.FIELDS_Y_POSITION_STEP;
        }
    }

    private void constructComplexFieldElements(Document document, Element root, Model model) {
        for (RelationshipField complexField : model.getComplexFields()) {
            if (complexField.getDrawable()) {
                String fieldElementId = complexField.getFieldId();
                String startArrow, endArrow;
                String startMultiplicity, endMultiplicity;

                String entryX = String.valueOf(complexField.getEntryX());
                String entryY = String.valueOf(complexField.getEntryY());
                String exitX = String.valueOf(complexField.getExitX());
                String exitY = String.valueOf(complexField.getExitY());

                String entryTextX;
                String entryTextY;
                String exitTextX;
                String exitTextY;

                if (complexField.getExitX() == 1)
                    exitTextX = "32";
                else exitTextX = "-32";

                if (complexField.getEntryX() == 1)
                    entryTextX = "32";
                else entryTextX = "-32";

                switch (complexField.getRelationshipFieldType()) {
                    case MANY2ONE:
                        startArrow = "diamondThin";
                        endArrow = "none";

                        startMultiplicity = "0..n";
                        endMultiplicity = "1..1";

                        exitTextY = "-15";
                        entryTextY = "-30";

                        break;
                    case MANY2MANY:
                        startArrow = "diamondThin";
                        endArrow = "diamondThin";

                        startMultiplicity = "0..n";
                        endMultiplicity = "0..n";

                        exitTextY = "-15";
                        entryTextY = "-15";

                        break;
                    case ONE2MANY:
                        startArrow = "none";
                        endArrow = "diamondThin";

                        startMultiplicity = "1..1";
                        endMultiplicity = "0..n";

                        exitTextY = "-30";
                        entryTextY = "-15";

                        break;
                    default:
                        startArrow = "none";
                        endArrow = "none";

                        startMultiplicity = "1..1";
                        endMultiplicity = "1..1";

                        exitTextY = "-30";
                        entryTextY = "-30";

                        break;
                }

                String color = this.getColorAsHex(complexField.getColor());

                Element fieldElement = document.createElement("mxCell");
                fieldElement.setAttribute("id", fieldElementId);
                fieldElement.setAttribute("value", "");
                fieldElement.setAttribute("parent", "1");
                fieldElement.setAttribute("source", model.getModelId());
                fieldElement.setAttribute("target", complexField.getTarget().getModelId());
                fieldElement.setAttribute("edge", "1");
                fieldElement.setAttribute("style", "edgeStyle=orthogonalEdgeStyle;" +
                        "endArrow=" + endArrow + ";" +
                        "startArrow=" + startArrow + ";" +
                        "html=1;" +
                        "endSize=64;" +
                        "startSize=64;" +
                        "startFill=0;" +
                        "exitX=" + exitX + ";" +
                        "exitY=" + exitY + ";" +
                        "exitDx=0;" +
                        "exitDy=0;" +
                        "entryX=" + entryX + ";" +
                        "entryY=" + entryY + ";" +
                        "entryDx=0;" +
                        "entryDy=0;" +
                        "fontSize=14;" +
                        "fontStyle=1;" +
                        "endFill=0;" +
                        "curved=1;" +
                        "sourcePerimeterSpacing=0;" +
                        "targetPerimeterSpacing=0;" +
                        "strokeColor=" + color + ";"
                );

                Element fieldElementPosition = document.createElement("mxGeometry");
                fieldElementPosition.setAttribute("relative", "1");
                fieldElementPosition.setAttribute("as", "geometry");

                this.constructMultiplicityElement(document, root, fieldElementId, "start_multiplicity_" + fieldElementId, startMultiplicity, "-1", exitTextX, exitTextY, color);
                this.constructMultiplicityElement(document, root, fieldElementId, "end_multiplicity_" + fieldElementId, endMultiplicity, "1", entryTextX, entryTextY, color);

                fieldElement.appendChild(fieldElementPosition);

                root.appendChild(fieldElement);
            }
        }
    }

    private void constructMultiplicityElement(Document document, Element root,
                                              String fieldElementId,
                                              String multiplicityFieldElementId,
                                              String multiplicityValue,
                                              String multiplicityElementPositionX,
                                              String multiplicityElementPositionPointX,
                                              String multiplicityElementPositionPointY,
                                              String colorAsHex) {

        Element multiplicityElement = document.createElement("mxCell");
        multiplicityElement.setAttribute("id", multiplicityFieldElementId);
        multiplicityElement.setAttribute("value", multiplicityValue);
        multiplicityElement.setAttribute("parent", fieldElementId);
        multiplicityElement.setAttribute("connectable", "0");
        multiplicityElement.setAttribute("vertex", "1");
        multiplicityElement.setAttribute("style", "resizable=0;" +
                "html=1;" +
                "align=center;" +
                "verticalAlign=top;" +
                "labelBackgroundColor=#ffffff;" +
                "fontSize=14;" +
                "fontStyle=1;" +
                "spacing=2;" +
                "fontColor=" + colorAsHex + ";");

        Element multiplicityElementPosition = document.createElement("mxGeometry");
        multiplicityElementPosition.setAttribute("x", multiplicityElementPositionX);
        multiplicityElementPosition.setAttribute("relative", "1");
        multiplicityElementPosition.setAttribute("as", "geometry");

        Element startMultiplicityElementPositionPoint = document.createElement("mxPoint");
        startMultiplicityElementPositionPoint.setAttribute("x", multiplicityElementPositionPointX);
        startMultiplicityElementPositionPoint.setAttribute("y", multiplicityElementPositionPointY);
        startMultiplicityElementPositionPoint.setAttribute("as", "offset");

        multiplicityElementPosition.appendChild(startMultiplicityElementPositionPoint);
        multiplicityElement.appendChild(multiplicityElementPosition);

        root.appendChild(multiplicityElement);
    }

    private String getColorAsHex(Color color) {
        String colorAsString = color.toString().replace("0x", "#");

        return colorAsString.substring(0, colorAsString.length() - 2);
    }
}
