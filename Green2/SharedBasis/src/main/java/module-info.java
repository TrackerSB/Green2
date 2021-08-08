module bayern.steinbrecher.green2.SharedBasis {
    exports bayern.steinbrecher.green2.sharedBasis.data;
    exports bayern.steinbrecher.green2.sharedBasis.elements;
    exports bayern.steinbrecher.green2.sharedBasis.people;
    exports bayern.steinbrecher.green2.sharedBasis.utility;

    requires bayern.steinbrecher.DBConnector;
    requires bayern.steinbrecher.SepaXMLGenerator;
    requires bayern.steinbrecher.Utility;
    requires io.soabase.recordbuilder.core;
    requires java.compiler;
    requires java.logging;
    requires java.prefs;
    requires java.xml;
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;

    requires static bayern.steinbrecher.GenericWizard;

    // FIXME Opens to whom?
    opens bayern.steinbrecher.green2.sharedBasis.data.language;
    opens bayern.steinbrecher.green2.sharedBasis.icons;
    opens bayern.steinbrecher.green2.sharedBasis.styles;
}