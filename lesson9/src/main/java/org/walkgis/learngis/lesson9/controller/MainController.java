package org.walkgis.learngis.lesson9.controller;

import de.felixroske.jfxsupport.FXMLController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.walkgis.learngis.lesson9.Lesson9Application;
import org.walkgis.learngis.lesson9.basicclasses.*;
import org.walkgis.learngis.lesson9.view.DataTableView;

import java.awt.Point;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;


@FXMLController
public class MainController implements Initializable {
    @Value(value = "${data.dir}")
    private String dataDir;
    @FXML
    private Canvas mainCanvas;
    @FXML
    private Button btnOpenShp, btnFullScreen, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight, btnAttributeTable;
    @FXML
    private Label lblPosition;

    private GISView view;
    private GISLayer layer;
    private Rectangle clientRectangle;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        clientRectangle = new Rectangle(0, 0, mainCanvas.getWidth(), mainCanvas.getHeight());
        view = new GISView(new GISExtent(new GISVertex(0, 0), new GISVertex(1, 1)), clientRectangle);
        mainCanvas.setOnMouseClicked(this::canvasClick);
        mainCanvas.setOnMouseMoved(this::canvasMouseMoved);
        btnZoomIn.setOnMouseClicked(this::mapActionClick);
        btnZoomOut.setOnMouseClicked(this::mapActionClick);
        btnMoveUp.setOnMouseClicked(this::mapActionClick);
        btnMoveDown.setOnMouseClicked(this::mapActionClick);
        btnMoveLeft.setOnMouseClicked(this::mapActionClick);
        btnMoveRight.setOnMouseClicked(this::mapActionClick);
        btnOpenShp.setOnMouseClicked(this::btnOpenShpClick);
        btnFullScreen.setOnMouseClicked(this::btnFullScreen);
        btnAttributeTable.setOnMouseClicked(this::btnAttributeTableClick);
    }

    @FXML
    private void btnAttributeTableClick(MouseEvent event) {
        Lesson9Application.showView(DataTableView.class, Modality.WINDOW_MODAL);
    }

    @FXML
    private void btnFullScreen(MouseEvent event) {
        if (layer == null) return;
        view.updateExtent(layer.extent);
        updateMap(true);
    }

    @FXML
    private void btnOpenShpClick(MouseEvent event) {
        GISShapefile gisShapefile = new GISShapefile();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("选择数据");
        Stage selectFile = new Stage();
        if (StringUtils.isEmpty(dataDir))
            dataDir = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(dataDir));

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("所有文件类型", "*.shp"),
                new FileChooser.ExtensionFilter("Shapefile", "*.shp")
        );
        File file = fileChooser.showOpenDialog(selectFile);
        if (file != null) {
            layer = gisShapefile.readShapefile(file.getAbsolutePath().substring(0, file.getAbsolutePath().lastIndexOf(".")));
            layer.drawAttributeOrNot = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("信息框");
            alert.setHeaderText("打开成功");
            alert.setContentText("read" + layer.featureCount() + " features");
            alert.showAndWait();

            view.updateExtent(layer.extent);
            updateMap(false);
        }
    }

    private void mapActionClick(MouseEvent event) {
        GISMapAction action = GISMapAction.zoomin;
        if (btnZoomIn == event.getSource()) action = GISMapAction.zoomin;
        else if (btnZoomOut == event.getSource()) action = GISMapAction.zoomout;
        else if (btnMoveUp == event.getSource()) action = GISMapAction.moveup;
        else if (btnMoveDown == event.getSource()) action = GISMapAction.movedown;
        else if (btnMoveLeft == event.getSource()) action = GISMapAction.movelet;
        else if (btnMoveRight == event.getSource()) action = GISMapAction.moveright;
        view.updateExtent(action);
        updateMap(true);
    }

    private void updateMap(boolean clear) {
        if (clear) {
            mainCanvas.getGraphicsContext2D().setFill(Color.WHITE);
            mainCanvas.getGraphicsContext2D().fillRect(0, 0, clientRectangle.getWidth(), clientRectangle.getHeight());
        }
        if (layer == null) return;
        layer.draw(mainCanvas.getGraphicsContext2D(), view);
    }

    @FXML
    private void canvasMouseMoved(MouseEvent event) {
        if (layer == null) return;
        GISVertex gisVertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
        lblPosition.setText(gisVertex.x + "," + gisVertex.y);
    }

    @FXML
    private void canvasClick(MouseEvent event) {
        if (layer == null) return;
        GISVertex gisVertex = view.toMapVertex(new Point((int) event.getX(), (int) event.getY()));
        GISSelect gisSelect = new GISSelect();
        if (gisSelect.select(gisVertex, layer.features, layer.shapeType, view) == SelectResult.OK) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);

            alert.setContentText(gisSelect.selectedFeature.getAttribute(4).toString());
            alert.showAndWait();
        }
    }

    @FXML
    public GISLayer getLayer() {
        return layer;
    }
}
