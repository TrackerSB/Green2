package bayern.steinbrecher.green2.programs.launcher.elements.report;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Represents a dialog showing a list of conditions and whether they are fullfilled.
 *
 * @author Stefan Huber
 * @since 2u14
 */
public class ConditionReport extends WizardPage<Optional<Boolean>, ConditionReportController> {

    private final Map<String, Callable<Boolean>> conditions;

    /**
     * Creates a new report showing and evaluating the given conditions.
     *
     * @param conditions The conditions to evaluate and show.
     */
    public ConditionReport(Map<String, Callable<Boolean>> conditions) {
        super("ConditionReport.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        this.conditions = conditions;
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setConditions(conditions);
    }
}
