package bayern.steinbrecher.green2.memberManagement.query;

import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.List;
import java.util.Optional;

/**
 * Represents a form for querying member.
 *
 * @author Stefan Huber
 */
public class Query extends WizardPage<Optional<List<List<String>>>, QueryController> {

    private final DBConnection dbConnection;

    /**
     * Creates a new query dialog which uses the given {@link DBConnection}.
     *
     * @param dbConnection The connection to use for queries.
     */
    public Query(DBConnection dbConnection) {
        super("query.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        this.dbConnection = dbConnection;
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setDbConnection(dbConnection);
    }
}
