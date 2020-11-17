module bayern.steinbrecher.green2.Uninstaller {
    exports bayern.steinbrecher.green2.uninstaller;

    requires bayern.steinbrecher.GenericWizard;
    requires bayern.steinbrecher.green2.SharedBasis;
    requires java.logging;
    requires java.prefs;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens bayern.steinbrecher.green2.uninstaller.confirmUninstall to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.uninstaller.deleteConfigs to bayern.steinbrecher.GenericWizard, javafx.fxml;
}
