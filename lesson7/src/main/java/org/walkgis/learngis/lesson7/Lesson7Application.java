package org.walkgis.learngis.lesson7;

import de.felixroske.jfxsupport.AbstractJavaFxApplicationSupport;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.walkgis.learngis.lesson7.view.InitSplash;
import org.walkgis.learngis.lesson7.view.MainView;

@SpringBootApplication
public class Lesson7Application extends AbstractJavaFxApplicationSupport {
    public static void main(String[] args) {
        launch(Lesson7Application.class, MainView.class, new InitSplash(), args);
    }

    @Override
    public void beforeInitialView(Stage stage, ConfigurableApplicationContext ctx) {
        stage.setTitle("地图切片");
    }
}
