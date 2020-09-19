module bayern.steinbrecher.green2.ConfigurationDialog {
    exports bayern.steinbrecher.green2.configurationDialog;

    requires bayern.steinbrecher.CheckedElements;
    requires bayern.steinbrecher.DBConnector;
    requires bayern.steinbrecher.GenericWizard;
    requires bayern.steinbrecher.green2.SharedBasis;
    requires bayern.steinbrecher.Utility;
    requires java.logging;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens bayern.steinbrecher.green2.configurationDialog to javafx.fxml;
    opens bayern.steinbrecher.green2.configurationDialog.elements to javafx.fxml;
}