package bayern.steinbrecher.green2.memberManagement.selection;

import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.wizard.WizardPage;
import com.google.common.collect.BiMap;
import javafx.scene.paint.Color;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a selection which allows to only select options but also to group them.
 *
 * @author Stefan Huber
 * @param <T> The type of the options to select.
 * @param <G> The type of the groups to associate items with.
 */
public final class SelectionGroup<T extends Comparable<T>, G>
        extends WizardPage<Optional<Map<T, G>>, SelectionGroupController<T, G>> {

    private final Set<T> options;
    private final BiMap<G, Color> groups;

    /**
     * Creates a new SelectionGroup for selecting and grouping the given options into the given groups.
     *
     * @param options The options to select.
     * @param groups The groups to group the options into.
     */
    public SelectionGroup(Set<T> options, BiMap<G, Color> groups) {
        super("SelectionGroup.fxml", EnvironmentHandler.RESOURCE_BUNDLE);
        this.options = options;
        this.groups = groups;
    }

    @Override
    protected void afterControllerInitialized() {
        getController().setGroups(groups);
        getController().setOptions(options);
    }
}
