module SalesSystem {
    // Frontend
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires jakarta.activation;

    opens frontend.cashflowFX to javafx.fxml;  //, java.xml.bind;
    exports frontend.cashflowFX;
    exports frontend.utils;
    exports frontend.interfaces;

    opens frontend.admin to javafx.graphics, javafx.fxml;
    exports frontend.admin;

    opens frontend.utils to javafx.fxml; // java.xml.bind,

    // backend
    requires org.json;
    requires spring.boot;
    requires spring.boot.autoconfigure;
    requires spring.web;
    requires spring.core;
    requires spring.beans;
    requires spring.context;
    //requires java.xml;
    //requires java.xml.bind;
    requires java.desktop;

    opens backend to spring.core;
    exports backend;

    // Shared
    exports sharedResources.productCatalog;
    exports sharedResources.utils;
    exports sharedResources.exceptions;
    exports sharedResources.structures;
    opens sharedResources.productCatalog to javafx.fxml; //java.xml.bind,
    opens sharedResources.utils to javafx.fxml; //java.xml.bind,
}