module com.myapp.recipemanagement {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires java.sql;
    requires java.desktop;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.core;
    requires jbcrypt;
    
    opens com.myapp to javafx.graphics, javafx.fxml;
    opens com.myapp.controllers to javafx.fxml;
    opens com.myapp.models to javafx.base;

    
    exports com.myapp;
    exports com.myapp.controllers;
    exports com.myapp.models;
    exports com.myapp.repositories;
    exports com.myapp.services;
    exports com.myapp.dao;
}
