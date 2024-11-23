module com.example.csc311_db_ui_semesterlongproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.azure.storage.blob;
    requires java.desktop;


    opens viewmodel;
    exports viewmodel;
    opens dao;
    exports dao;
    opens model;
    exports model;
}