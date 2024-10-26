module org.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires com.fazecast.jSerialComm;
    requires static lombok;
    requires java.logging;

    opens org.example.demo to javafx.fxml;
    exports org.example.demo;
    exports org.example.demo.ui;
    opens org.example.demo.ui to javafx.fxml;
    exports org.example.demo.handler;
    opens org.example.demo.handler to javafx.fxml;
    exports org.example.demo.service;
    opens org.example.demo.service to javafx.fxml;
}