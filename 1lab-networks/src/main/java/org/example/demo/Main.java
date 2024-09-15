package org.example.demo;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class Main extends Application {

    private static final ArrayList<String> devicesList = new ArrayList<>();

    public static void main(String[] args) {
        loadDevices();
        launch(args);
    }

    private static void loadDevices() {
        File devDir = new File("/dev");
        File[] devices = devDir.listFiles((dir, name) -> name.startsWith("ttys") && name.length() == 7);
        assert devices != null;
        for (File device : devices) {
            devicesList.add(device.getName());
        }
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Networks");
        MainUI controller = new MainUI(primaryStage, devicesList);
        controller.initializeUI();
    }
}