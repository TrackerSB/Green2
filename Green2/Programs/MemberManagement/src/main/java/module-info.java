module bayern.steinbrecher.green2.MemberManagement {
    exports bayern.steinbrecher.green2.memberManagement;

    requires bayern.steinbrecher.CheckedElements;
    requires bayern.steinbrecher.DBConnector;
    requires bayern.steinbrecher.GenericWizard;
    requires bayern.steinbrecher.green2.SharedBasis;
    requires bayern.steinbrecher.SepaXMLGenerator;
    requires bayern.steinbrecher.Utility;
    requires com.google.common;
    requires java.desktop;
    requires java.logging;
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires org.jetbrains.annotations;

    opens bayern.steinbrecher.green2.memberManagement.contribution to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.elements to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.login.simple to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.login.ssh to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.menu to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.query to bayern.steinbrecher.GenericWizard, javafx.fxml;
    opens bayern.steinbrecher.green2.memberManagement.sepaform to bayern.steinbrecher.GenericWizard, javafx.fxml;
}