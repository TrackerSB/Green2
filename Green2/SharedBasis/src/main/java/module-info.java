module bayern.steinbrecher.green2.SharedBasis {
    exports bayern.steinbrecher.green2.sharedBasis.data;
    exports bayern.steinbrecher.green2.sharedBasis.elements;
    exports bayern.steinbrecher.green2.sharedBasis.people;
    exports bayern.steinbrecher.green2.sharedBasis.utility;

    requires bayern.steinbrecher.DBConnector;
    requires bayern.steinbrecher.Utility;
    requires com.google.errorprone.annotations;
    requires java.logging;
    requires java.prefs;
    requires java.xml;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
}