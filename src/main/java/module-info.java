module jaywalk {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;

    opens jaywalk to javafx.fxml;
    exports jaywalk;
}