package bayern.steinbrecher.green2.memberManagement.result;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.List;
import java.util.Optional;

/**
 * A dialog for displaying tables of {@link String}s representing results of queries.
 *
 * @author Stefan Huber
 */
public class ResultDialog extends WizardPage<Optional<Void>, ResultDialogController> {

    private final List<List<String>> results;

    /**
     * Creates a new {@link ResultDialog} initially showing the content of {@code results}. It is assumed that the first
     * line contains the headings for the columns.
     *
     * @param results The content to show initially.
     */
    public ResultDialog(List<List<String>> results) {
        super("ResultDialog.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        this.results = results;
    }

    @Override
    protected void afterControllerInitialized() {
        setResults(results);
    }

    /**
     * Sets the results to show.
     *
     * @param results The results to show.
     */
    public void setResults(List<List<String>> results) {
        getController().setResults(results);
    }
}
