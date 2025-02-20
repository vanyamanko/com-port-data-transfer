package org.example.demo;

import java.io.File;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.example.demo.ui.MainUI;

public class Main extends Application {

    private static final ArrayList<String> devicesList = new ArrayList<>();

    public static void main(String[] args) {
        loadDevices();
        launch(args);
    }

    private static void loadDevices() {
        File devDir = new File("/dev");
        File[] devices = devDir.listFiles(
            (dir, name) -> name.startsWith("ttys") && name.length() == 7
        );
        assert devices != null;
        for (File device : devices) {
            devicesList.add(device.getName());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        Pane mainWindow = new Pane();
        Scene scene = new Scene(mainWindow, 1000, 750);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setWidth(1000);
        primaryStage.setHeight(750);
        primaryStage.setTitle("com-port-data-transfer");
        MainUI controller = new MainUI(primaryStage, devicesList);
        controller.initializeUI();
    }
}
