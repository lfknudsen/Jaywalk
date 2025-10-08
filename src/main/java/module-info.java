module com.falkknudsen.jaywalk {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires java.xml;
    requires org.apache.commons.compress;
    requires org.apache.commons.io;

    opens com.falkknudsen.jaywalk to javafx.fxml;
    exports com.falkknudsen.jaywalk;
    exports com.falkknudsen.jaywalk.contracts;
    opens com.falkknudsen.jaywalk.contracts to javafx.fxml;
    exports com.falkknudsen.jaywalk.util;
    opens com.falkknudsen.jaywalk.util to javafx.fxml;
    exports com.falkknudsen.jaywalk.structs;
    opens com.falkknudsen.jaywalk.structs to javafx.fxml;
}