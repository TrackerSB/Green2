package bayern.steinbrecher.green2.memberManagement.menu;

import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.utility.StagePreparer;
import bayern.steinbrecher.wizard.StandaloneWizardPage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

/**
 * Represents the main menu containing the main functions.
 *
 * @author Stefan Huber
 */
public class MainMenu extends StandaloneWizardPage<Optional<Void>, MainMenuController> implements StagePreparer {

    private final DBConnection dbConnection;

    /**
     * Creates a Menu which contains controls for all the functionality to be used by the user.
     *
     * @param dbConnection The connection to use for querying data.
     */
    public MainMenu(DBConnection dbConnection) {
        super("MainMenu.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        this.dbConnection = dbConnection;
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setDbConnection(dbConnection);
    }

    @Override
    public Collection<String> getRegisteredStylesheets() {
        Collection<String> stylesheets = new ArrayList<>(StagePreparer.super.getRegisteredStylesheets());
        stylesheets.add(getClass().getResource("MainMenu.css").toExternalForm());
        return stylesheets;
    }
}
