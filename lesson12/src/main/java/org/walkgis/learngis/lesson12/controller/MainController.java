package org.walkgis.learngis.lesson12.controller;

import de.felixroske.jfxsupport.AbstractFxmlView;
import de.felixroske.jfxsupport.FXMLController;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.util.StringUtils;
import org.walkgis.learngis.lesson12.Lesson12Application;
import org.walkgis.learngis.lesson12.basicclasses.*;
import org.walkgis.learngis.lesson12.view.DataTableView;

import java.awt.*;
import java.awt.image.BufferedImage;
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
    private Button btnOpenShp, btnFullScreen, btnZoomIn, btnZoomOut, btnMoveUp, btnMoveDown, btnMoveLeft, btnMoveRight, btnAttributeTable, btnClear;
    @FXML
    private Label lblPosition, lblCount;
    @Autowired
    private DataTableController dataTableController;
    @Autowired
    private ApplicationContext applicationContext;
    private BufferedImage backgroundWindow;

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
        btnClear.setOnMouseClicked(this::btnClearClick);
    }

    @FXML
    private void btnClearClick(MouseEvent mouseEvent) {
        if (layer == null) return;
        layer.clearSelection();
        updateMap();
        //更新状态栏
        lblCount.setText("当前选中：0");
        updateAttributeWindow();
    }

    private void updateAttributeWindow() {
        if (layer == null) return;
        if (dataTableController == null) return;
        dataTableController.updateData();
    }

    @FXML
    private void btnAttributeTableClick(MouseEvent event) {
        AbstractFxmlView view = applicationContext.getBean(DataTableView.class);
        Stage newStage = new Stage();
        Scene newScene;
        if (view.getView().getScene() != null) {
            newScene = view.getView().getScene();
        } else {
            newScene = new Scene(view.getView());
        }

        newStage.setScene(newScene);
        newStage.initModality(Modality.NONE);
        newStage.initOwner(Lesson12Application.getStage());
        newStage.show();
        dataTableController.initTable();
    }

    @FXML
    private void btnFullScreen(MouseEvent event) {
        if (layer == null) return;
        view.updateExtent(layer.extent);
        updateMap();
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
            updateMap();
        }
    }

    @FXML
    private void mapActionClick(MouseEvent event) {
        GISMapAction action = GISMapAction.zoomin;
        if (btnZoomIn == event.getSource()) action = GISMapAction.zoomin;
        else if (btnZoomOut == event.getSource()) action = GISMapAction.zoomout;
        else if (btnMoveUp == event.getSource()) action = GISMapAction.moveup;
        else if (btnMoveDown == event.getSource()) action = GISMapAction.movedown;
        else if (btnMoveLeft == event.getSource()) action = GISMapAction.movelet;
        else if (btnMoveRight == event.getSource()) action = GISMapAction.moveright;
        view.updateExtent(action);
        updateMap();
    }

    void updateMap() {
        if (layer == null) return;
        if (clientRectangle.getWidth() * clientRectangle.getHeight() == 0) return;
        view.updateRectangle(clientRectangle);

        if (backgroundWindow != null) backgroundWindow = null;
        backgroundWindow = new BufferedImage((int) clientRectangle.getWidth(), (int) clientRectangle.getHeight(), BufferedImage.TYPE_INT_ARGB);

        //背景窗口上绘图
        Graphics2D graphics = (Graphics2D) backgroundWindow.getGraphics();
        graphics.setBackground(Color.WHITE);
        graphics.clearRect(0, 0, (int) clientRectangle.getWidth(), (int) clientRectangle.getHeight());
        layer.draw(graphics, view);

        WritableImage image = SwingFXUtils.toFXImage(backgroundWindow, null);
        mainCanvas.getGraphicsContext2D().drawImage(image, 0, 0);
        lblCount.setText("当前选中：" + layer.selection.size());
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
        SelectResult selectResult = layer.select(gisVertex, view);
        if (selectResult == SelectResult.OK) {
            updateMap();
            //更新状态栏
            lblCount.setText("当前选中：" + layer.selection.size());
            updateAttributeWindow();
        }
    }

    @FXML
    public GISLayer getLayer() {
        return layer;
    }
}
