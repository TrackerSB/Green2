package bayern.steinbrecher.green2.memberManagement.utility;

import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Map;

public final class CheckReportDialogUtility {
    private CheckReportDialogUtility(){}

    public static Parent createCheckReportDialog(Stage stage, Map<String, List<String>> reports) {
        VBox reportsBox = new VBox();
        reports.entrySet().stream()
                .map(entry -> {
                    VBox messagesBox = new VBox();
                    List<String> messages = entry.getValue();
                    messages.stream()
                            .map(message -> new Label(message))
                            .forEach(messagesBox.getChildren()::add);
                    TitledPane reportPane = new TitledPane(entry.getKey() + " (" + messages.size() + ")", messagesBox);
                    reportPane.setCollapsible(!messages.isEmpty());
                    reportPane.setExpanded(false);
                    return reportPane;
                })
                .forEach(reportsBox.getChildren()::add);

        ScrollPane scrollableReportsBox = new ScrollPane(reportsBox);
        HBox.setHgrow(scrollableReportsBox, Priority.ALWAYS);
        return scrollableReportsBox;
    }
}
