package bayern.steinbrecher.green2.memberManagement.menu;

import bayern.steinbrecher.checkedElements.spinner.CheckedIntegerSpinner;
import bayern.steinbrecher.dbConnector.DBConnection;
import bayern.steinbrecher.dbConnector.query.GenerationFailedException;
import bayern.steinbrecher.dbConnector.query.QueryFailedException;
import bayern.steinbrecher.dbConnector.scheme.TableScheme;
import bayern.steinbrecher.green2.memberManagement.contribution.Contribution;
import bayern.steinbrecher.green2.memberManagement.generator.AddressGenerator;
import bayern.steinbrecher.green2.memberManagement.generator.BirthdayGenerator;
import bayern.steinbrecher.green2.memberManagement.generator.sepa.SepaPain00800302XMLGenerator;
import bayern.steinbrecher.green2.memberManagement.generator.sepa.SequenceType;
import bayern.steinbrecher.green2.memberManagement.people.Originator;
import bayern.steinbrecher.green2.memberManagement.query.Query;
import bayern.steinbrecher.green2.memberManagement.result.ResultDialog;
import bayern.steinbrecher.green2.memberManagement.selection.SelectionGroup;
import bayern.steinbrecher.green2.memberManagement.sepaform.SepaForm;
import bayern.steinbrecher.green2.memberManagement.utility.CheckReportDialogUtility;
import bayern.steinbrecher.green2.sharedBasis.data.AppInfo;
import bayern.steinbrecher.green2.sharedBasis.data.EnvironmentHandler;
import bayern.steinbrecher.green2.sharedBasis.data.ProfileSettings;
import bayern.steinbrecher.green2.sharedBasis.data.Tables;
import bayern.steinbrecher.green2.sharedBasis.people.Member;
import bayern.steinbrecher.green2.sharedBasis.utility.IOStreamUtility;
import bayern.steinbrecher.green2.sharedBasis.utility.PreparationUtility;
import bayern.steinbrecher.javaUtility.DialogCreationException;
import bayern.steinbrecher.javaUtility.DialogUtility;
import bayern.steinbrecher.javaUtility.SepaUtility;
import bayern.steinbrecher.wizard.EmbeddedWizardPage;
import bayern.steinbrecher.wizard.StandaloneWizardPageController;
import bayern.steinbrecher.wizard.Wizard;
import bayern.steinbrecher.wizard.WizardState;
import bayern.steinbrecher.wizard.pages.Selection;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.LoadException;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Stefan Huber
 */
public class MainMenuController extends StandaloneWizardPageController<Optional<Void>> {

    private static final Logger LOGGER = Logger.getLogger(MainMenuController.class.getName());
    private static final DateTimeFormatter COMPILE_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
            .withZone(ZoneId.systemDefault());
    private static final int CURRENT_YEAR = LocalDate.now().getYear();
    private Stage stage;
    private DBConnection dbConnection;
    private final ObjectProperty<Optional<LocalDateTime>> dataLastUpdated
            = new SimpleObjectProperty<>(Optional.empty());
    private final CompletableFutureProperty<Set<Member>> member = new CompletableFutureProperty<>();
    private final CompletableFutureProperty<Map<String, String>> nicknames = new CompletableFutureProperty<>();
    private final BooleanProperty allDataAvailable = new SimpleBooleanProperty(this, "allDataAvailable");
    private final BooleanProperty activateBirthdayFeatures
            = new SimpleBooleanProperty(this, "activateBirthdayFeatures", true);

    @FXML
    private MenuItem generateAddressesBirthdayItem;
    @FXML
    private MenuItem generateBirthdayInfosItem;
    @FXML
    private CheckedIntegerSpinner yearSpinner;
    @FXML
    private CheckedIntegerSpinner yearSpinner2;
    @FXML
    private CheckedIntegerSpinner yearSpinner3;
    @FXML
    private javafx.scene.control.Menu honoringsMenu;
    private final ListProperty<MenuItem> addedHonorings = new SimpleListProperty<>(FXCollections.observableArrayList());
    @FXML
    private javafx.scene.control.Menu licensesMenu;
    @FXML
    private Label dataLastUpdatedLabel;
    @FXML
    private Rectangle overlayBackground;

    /**
     * Binds the textual representation of the year spinners to text properties of elements in the menu.
     */
    private void bindYearSpinnerTo() {
        StringBinding yearBinding = Bindings.createStringBinding(
                () -> yearSpinner.isValid() ? yearSpinner.getValue().toString() : "?",
                yearSpinner.validProperty(), yearSpinner.valueProperty());

        generateBirthdayInfosItem.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("groupedBirthdayMember") + " ")
                        .concat(yearBinding));
        generateAddressesBirthdayItem.textProperty().bind(
                new SimpleStringProperty(EnvironmentHandler.getResourceValue("birthdayExpression") + " ")
                        .concat(yearBinding));
    }

    private void bindAvailabilityInformations() {
        allDataAvailable.bind(member.availableProperty()
                .and(nicknames.availableProperty()));
        dataLastUpdatedLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            Optional<LocalDateTime> dataLastUpdatedOptional = getDataLastUpdated();
            String text;
            if (dataLastUpdatedOptional.isPresent()) {
                if (dataLastUpdatedOptional.get().equals(LocalDateTime.MIN)) {
                    text = EnvironmentHandler.getResourceValue("dataQueryFailed");
                } else {
                    text = EnvironmentHandler.getResourceValue(
                            "dataLastUpdated", dataLastUpdatedOptional.get()
                                    .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
                }
            } else {
                text = EnvironmentHandler.getResourceValue("noData");
            }
            return text;
        }, dataLastUpdatedProperty()));
    }

    private void showHonorings(int yearsOfMembership) {
        if (yearSpinner.isValid()) {
            List<List<String>> result = new ArrayList<>();
            result.add(List.of(
                    EnvironmentHandler.getResourceValue("membershipNumber"),
                    EnvironmentHandler.getResourceValue("prename"),
                    EnvironmentHandler.getResourceValue("lastname"),
                    EnvironmentHandler.getResourceValue("memberSince"),
                    EnvironmentHandler.getResourceValue("isActive")
            ));
            result.addAll(streamCurrentMember()
                    .filter(m -> !m.getHonorings().getOrDefault(yearsOfMembership, Boolean.FALSE))
                    .filter(m -> yearSpinner.getValue() - m.getMemberSince().getYear() >= yearsOfMembership)
                    .map(
                            m -> List.of(Integer.toString(m.getMembershipnumber()),
                                    m.getPerson().getPrename(),
                                    m.getPerson().getLastname(),
                                    m.getMemberSince().toString(),
                                    m.isActive().map(Object::toString).orElse("")))
                    .collect(Collectors.toList())
            );
            try {
                Pane resultDialog = new ResultDialog(result)
                        .generateEmbeddableWizardPage()
                        .getRoot();
                Stage resultStage = new Stage();
                resultStage.setScene(new Scene(resultDialog));
                resultStage.show();
            } catch (LoadException ex) {
                LOGGER.log(Level.SEVERE, "Could not create result dialog", ex);
            }
        }
    }

    private void generateHonoringsMenu() {
        member.availableProperty().addListener((obs, wereAvailable, areAvailable) -> {
            honoringsMenu.getItems().removeAll(addedHonorings);
            //TODO Is the following command guaranteed to run before new items are added?
            Platform.runLater(addedHonorings::clear);

            streamCurrentMember()
                    .map(Member::getHonorings)
                    .flatMap(h -> h.keySet().stream())
                    .distinct()
                    .sorted()
                    .forEach(year -> {
                        String membershipTitle = EnvironmentHandler.getResourceValue("yearsMembership", year);
                        MenuItem membershipItem = new MenuItem(membershipTitle);
                        membershipItem.setOnAction(aevt -> showHonorings(year));
                        membershipItem.disableProperty()
                                .bind(yearSpinner.validProperty().not());
                        Platform.runLater(() -> {
                            honoringsMenu.getItems()
                                    .add(membershipItem);
                            addedHonorings.add(membershipItem);
                        });
                    });
        });
    }

    private void generateLicensesMenu() {
        EnvironmentHandler.getLicenses().forEach(license -> {
            MenuItem item = new MenuItem(license.getName());
            item.setOnAction(aevt -> {
                try {
                    boolean changedWritable = license.setWritable(false, false);
                    if (!changedWritable) {
                        LOGGER.log(Level.WARNING, "Could not disable write permission for license files");
                    }
                    Desktop.getDesktop().open(license);
                } catch (IOException ex) {
                    LOGGER.log(Level.WARNING, "Could not open license", ex);
                }
            });
            licensesMenu.getItems().add(item);
        });
    }

    @FXML
    public void initialize() {
        yearSpinner2.valueFactoryProperty().bind(yearSpinner.valueFactoryProperty());
        yearSpinner3.valueFactoryProperty().bind(yearSpinner.valueFactoryProperty());
        yearSpinner.getValueFactory().setValue(CURRENT_YEAR + 1);

        bindYearSpinnerTo();
        bindAvailabilityInformations();

        //Bind activateBirthdayFeatures
        activateBirthdayFeatures.bind(Bindings.createBooleanBinding(
                () -> EnvironmentHandler.getProfile().get(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES),
                EnvironmentHandler.loadedProfileProperty(),
                EnvironmentHandler.getProfile().getProperty(ProfileSettings.ACTIVATE_BIRTHDAY_FEATURES)));

        generateHonoringsMenu();
        generateLicensesMenu();
    }

    public void setStage(Stage stage) {
        overlayBackground.widthProperty().bind(stage.widthProperty());
        overlayBackground.heightProperty().bind(stage.heightProperty());
        this.stage = stage;
    }

    /**
     * Sets the connection to use for querying data and queries immediatly for new data using the given connection.
     *
     * @param dbConnection The connection to use for querying data.
     */
    public void setDbConnection(@NotNull DBConnection dbConnection) {
        this.dbConnection = Objects.requireNonNull(dbConnection, "The connection must not be null.");
        queryData();
    }

    private void showNoMemberForOutputDialog() {
        String noMemberForOutput = EnvironmentHandler.getResourceValue("noMemberForOutput");
        try {
            DialogUtility.showAndWait(DialogUtility.createInfoAlert(noMemberForOutput, noMemberForOutput));
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not inform user graphically that no member were found", ex);
        }
    }

    private void generateAddresses(Collection<Member> requestedMember, File outputFile)
            throws IOException, InterruptedException, ExecutionException {
        if (requestedMember.isEmpty()) {
            throw new IllegalArgumentException("Passed empty list to generateAddresses(...)");
        }
        IOStreamUtility.printContent(
                AddressGenerator.generateAddressData(requestedMember, nicknames.get().get()), outputFile, true);
    }

    /**
     * Generates a file Serienbrief_alle.csv containing addresses of all member.
     */
    public void generateAddressesAll() {
        try {
            Set<Member> memberList = streamCurrentMember().collect(Collectors.toSet());
            if (memberList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                Optional<File> path = EnvironmentHandler.askForSavePath(stage, "serialLetterAll", "csv");
                if (path.isPresent()) {
                    generateAddresses(memberList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not generate addresses.", ex);
        }
    }

    /**
     * Generates a file Serienbrief_Geburtstag_{@code year}.csv containing addresses of all member who get a birthday
     * notification in year {@code year}.
     *
     * @param year The year to look for member.
     */
    public void generateAddressesBirthday(int year) {
        try {
            Set<Member> memberBirthdayList = streamBirthdayMembers(year)
                    .collect(Collectors.toSet());
            if (memberBirthdayList.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                Optional<File> path
                        = EnvironmentHandler.askForSavePath(stage, "serialLetterBirthday", "csv", year);
                if (path.isPresent()) {
                    generateAddresses(memberBirthdayList, path.get());
                }
            }
        } catch (InterruptedException | ExecutionException | IOException ex) {
            LOGGER.log(Level.SEVERE, "Could not generate addresses.", ex);
        }
    }

    //TODO Where to place this method? How to generalize it for all optional columns?
    private boolean isContributionColumnEnabled() {
        return streamCurrentMember()
                .anyMatch(m -> m.getContribution().isPresent());
    }

    private void generateSepa(Stream<Member> memberToSelectFuture, boolean useMemberContributions,
                              SequenceType sequenceType) {
        try {
            Set<Member> memberToSelect = memberToSelectFuture.collect(Collectors.toSet());

            if (memberToSelect.isEmpty()) {
                showNoMemberForOutputDialog();
            } else {
                boolean askForContribution = !(useMemberContributions && isContributionColumnEnabled());

                EmbeddedWizardPage<Optional<Originator>> sepaFormPage = new SepaForm().generateEmbeddableWizardPage();
                sepaFormPage.setFinishAndNext(false, () -> askForContribution ? "contribution" : "selection");
                EmbeddedWizardPage<Optional<BiMap<Double, Color>>> contributionPage
                        = new Contribution().generateEmbeddableWizardPage();
                EmbeddedWizardPage<Optional<Set<Member>>> selectionPage
                        = new Selection<>(memberToSelect).generateEmbeddableWizardPage();
                selectionPage.setFinishAndNext(true, null);

                Map<String, EmbeddedWizardPage<?>> pages = new HashMap<>();
                pages.put(EmbeddedWizardPage.FIRST_PAGE_KEY, sepaFormPage);
                pages.put("contribution", contributionPage);
                pages.put("selection", selectionPage);
                Wizard wizard = Wizard.create(pages);

                CompletableFuture<EmbeddedWizardPage<Optional<Map<Member, Double>>>> selectionGroupPage
                        = contributionPage.setFinishAndDynamicNext(
                        false, () -> new SelectionGroup<>(new HashSet<>(memberToSelect),
                                contributionPage.getResult().orElse(HashBiMap.create())), "selectionGroup")
                        .thenApply(page -> {
                            page.setFinishAndNext(true, null);
                            return page;
                        });
                wizard.stateProperty().addListener((obs, oldVal, newVal) -> {
                    if (newVal == WizardState.FINISHED) {
                        Set<Member> selectedMember;
                        if (askForContribution) {
                            Optional<Map<Member, Double>> groupedMember;
                            try {
                                groupedMember = selectionGroupPage.get()
                                        .getResult();
                            } catch (InterruptedException | ExecutionException ex) {
                                LOGGER.log(Level.SEVERE, "Could not display group selection page", ex);
                                groupedMember = Optional.empty();
                            }
                            selectedMember = groupedMember.orElseThrow()
                                    .entrySet()
                                    .stream()
                                    .map(entry -> new Member.Builder(entry.getKey())
                                            .setContribution(Optional.ofNullable(entry.getValue()))
                                            .generate())
                                    .collect(Collectors.toSet());
                        } else {
                            selectedMember = selectionPage.getResult()
                                    .orElseThrow();
                        }
                        Originator originator = sepaFormPage.getResult()
                                .orElseThrow();

                        EnvironmentHandler.askForSavePath(stage, "sepa", "xml")
                                .ifPresent(file -> {
                                    try {
                                        List<Member> invalidMember
                                                = SepaPain00800302XMLGenerator.createXMLFile(selectedMember, originator,
                                                sequenceType, file, EnvironmentHandler.getProfile()
                                                        .getOrDefault(ProfileSettings.SEPA_USE_BOM, true));
                                        String message = invalidMember.stream()
                                                .map(Member::toString)
                                                .collect(Collectors.joining("\n"));
                                        if (!message.isEmpty()) {
                                            try {
                                                Alert alert = DialogUtility.createErrorAlert(message + "\n"
                                                        + EnvironmentHandler
                                                        .getResourceValue("haveBadAccountInformation"));
                                                Platform.runLater(alert::show);
                                            } catch (DialogCreationException ex) {
                                                LOGGER.log(
                                                        Level.WARNING,
                                                        "Could not inform user graphically that some member have "
                                                                + "invalid account information",
                                                        ex);
                                            }
                                        }
                                    } catch (IOException ex) {
                                        LOGGER.log(Level.SEVERE, "The sepa xml file could not be created.", ex);
                                    }
                                });
                    }
                });
                Stage wizardStage = new Stage();
                wizard.stateProperty()
                        .addListener((obs, previousState, currentState) -> {
                            if (currentState == WizardState.FINISHED
                                    || currentState == WizardState.ABORTED) {
                                wizardStage.close();
                            }
                        });
                wizardStage.initOwner(stage);
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("generateSepa"));
                wizardStage.setResizable(false);
                Scene wizardScene = PreparationUtility.addStyle(new Scene(wizard.getRoot()));
                wizardStage.setScene(wizardScene);
                PreparationUtility.addLogo(wizardStage)
                        .showAndWait();
            }
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            String noSepaDebit = EnvironmentHandler.getResourceValue("noSepaDebit");
            try {
                DialogUtility.showAndWait(DialogUtility.createStacktraceAlert(ex, noSepaDebit, noSepaDebit));
            } catch (DialogCreationException exx) {
                LOGGER.log(Level.WARNING, "Could not show stacktrace graphically to user", exx);
            }
        }
    }

    /**
     * Disables the node which {@code aevt} is belonging to, runs {@code run} and enables it again.
     *
     * @param aevt The event of the control which calls {@code run}.
     * @param run  The method to call.
     */
    private void callOnDisabled(EventObject aevt, Runnable run) {
        Object sourceObj = aevt.getSource();
        Class<?> sourceClass = sourceObj.getClass();
        if (sourceClass.isAssignableFrom(Node.class) || sourceClass.isAssignableFrom(MenuItem.class)) {
            try {
                Method disablePropertyMethod = sourceClass.getMethod("disableProperty");
                Method isBoundMethod = disablePropertyMethod.getReturnType().getMethod("isBound");
                if ((boolean) isBoundMethod.invoke(disablePropertyMethod.invoke(sourceObj))) {
                    //When getting here: sourceObj.disableProperty().isBound() == true
                    LOGGER.log(Level.WARNING,
                            "Cannot disable control {0} since its DisableProperty is bound.", sourceObj);
                    run.run();
                } else {
                    Method setDisableMethod = sourceClass.getMethod("setDisable", Boolean.TYPE);
                    setDisableMethod.invoke(sourceObj, true);
                    run.run();
                    setDisableMethod.invoke(sourceObj, false);
                }
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        } else {
            LOGGER.log(
                    Level.WARNING, "The source of the ActionEvent is no Node and no MenuItem. It can´t be disabled.");
            run.run();
        }
    }

    private <T> CompletableFuture<T> getSupplyTableContentFuture(TableScheme<T, ?> scheme) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return dbConnection.getTableContent(scheme);
            } catch (GenerationFailedException | QueryFailedException ex) {
                throw new CompletionException(
                        String.format("Could not get table content of '%s'", scheme.getTableName()), ex);
            }
        });
    }

    @FXML
    private void queryData() {
        member.set(getSupplyTableContentFuture(Tables.MEMBER));
        nicknames.set(getSupplyTableContentFuture(Tables.NICKNAMES));
        allDataAvailableProperty()
                .addListener((obs, allWereAvailable, allAreAvailable) -> {
                    if (allAreAvailable) {
                        Platform.runLater(() -> dataLastUpdated.set(Optional.of(LocalDateTime.now())));
                    }
                });
    }

    @FXML
    @SuppressWarnings("unused")
    private void openQueryDialog(ActionEvent aevt) {
        callOnDisabled(aevt, () -> {
            try {
                Map<String, EmbeddedWizardPage<?>> pages = new HashMap<>();
                EmbeddedWizardPage<Optional<List<List<String>>>> queryDialogPage = new Query(dbConnection)
                        .generateEmbeddableWizardPage();
                pages.put(EmbeddedWizardPage.FIRST_PAGE_KEY, queryDialogPage);
                Wizard queryWizard = Wizard.create(pages);
                queryDialogPage.setFinishAndNext(false, () -> {
                    try {
                        EmbeddedWizardPage<Optional<Void>> queryResultPage
                                = new ResultDialog(queryDialogPage.getResult().orElse(new ArrayList<>()))
                                .generateEmbeddableWizardPage();
                        queryResultPage.setFinishAndNext(true, null);
                        queryWizard.putPage("queryResult", queryResultPage);
                        return "queryResult";
                    } catch (LoadException ex) {
                        throw new Error("Could not generate wizard page showing the query result", ex);
                    }
                });
                Stage wizardStage = new Stage();
                wizardStage.initOwner(stage);
                wizardStage.initModality(Modality.WINDOW_MODAL);
                wizardStage.setTitle(EnvironmentHandler.getResourceValue("queryData"));
                wizardStage.setResizable(true);
                Scene wizardScene = PreparationUtility.addStyle(new Scene(queryWizard.getRoot()));
                wizardStage.setScene(wizardScene);
                queryWizard.stateProperty()
                        .addListener((obs, previousState, currentState) -> {
                            if (currentState == WizardState.ABORTED
                                    || currentState == WizardState.FINISHED) {
                                wizardStage.close();
                            }
                        });
                PreparationUtility.addLogo(wizardStage)
                        .show();
            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
                try {
                    DialogUtility.createStacktraceAlert(
                            ex, EnvironmentHandler.getResourceValue("noQueryDialog"))
                            .showAndWait();
                } catch (DialogCreationException exx) {
                    LOGGER.log(Level.WARNING, "Could not show stacktrace graphically to user", exx);
                }
            }
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateContributionSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(streamNonContributionFree(), true, SequenceType.RCUR));
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateUniversalSepa(ActionEvent aevt) {
        callOnDisabled(aevt, () -> generateSepa(streamCurrentMember(), false, SequenceType.RCUR));
    }

    private List<String> checkIbans() {
        String noIban = EnvironmentHandler.getResourceValue("noIban");
        return streamCurrentMember()
                .filter(m -> !SepaUtility.isValidIban(m.getAccountHolder().getIban()))
                .map(m -> {
                    String iban = m.getAccountHolder().getIban();
                    return m + ": \"" + (iban.isEmpty() ? noIban : iban) + "\"";
                })
                .collect(Collectors.toList());
    }

    private List<String> checkBics() {
        String noBic = EnvironmentHandler.getResourceValue("noBic");
        return streamCurrentMember()
                .filter(m -> !SepaUtility.isValidBic(m.getAccountHolder().getBic()))
                .map(m -> {
                    String bic = m.getAccountHolder().getBic();
                    return m + ": \"" + (bic.isEmpty() ? noBic : bic) + "\"";
                })
                .collect(Collectors.toList());
    }

    private List<String> checkDates(Stream<Member> memberToCheck, Function<Member, LocalDate> dateFunction) {
        return memberToCheck.map(m -> new Pair<>(m.toString(), dateFunction.apply(m)))
                .filter(p -> p.getValue() == null)
                .map(p -> p.getKey()
                        + ": \"" + DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(p.getValue())
                        + "\""
                )
                .collect(Collectors.toList());
    }

    private List<String> checkBirthdays() {
        return checkDates(streamCurrentMember(), m -> m.getPerson().getBirthday());
    }

    private List<String> checkMandateSigned() {
        return checkDates(streamCurrentMember(), m -> m.getAccountHolder().getMandateSigned());
    }

    private List<String> checkContributions() {
        List<String> invalidContributions;
        if (isContributionColumnEnabled()) {
            invalidContributions = streamCurrentMember()
                    .filter(m -> {
                        Optional<Double> contribution = m.getContribution();
                        return contribution.isEmpty() || contribution.get() < 0
                                || contribution.get() == 0 && !m.isContributionfree();
                    })
                    .map(m -> m.toString() + ": " + m.getContribution())
                    .collect(Collectors.toList());
        } else {
            invalidContributions = new ArrayList<>();
        }
        return invalidContributions;
    }

    @FXML
    @SuppressWarnings("unused")
    private void checkData(ActionEvent aevt) {
        /*
         * Maps resource keys ({@link EnvironmentHandler#getResourceValue(java.lang.String, java.lang.Object...)} to
         * functions generating checks.
         */
        Map<String, Callable<List<String>>> checkFunctions = Map.of(
                "iban", this::checkIbans,
                "bic", this::checkBics,
                "birthdays", this::checkBirthdays,
                "columnMandatSigned", this::checkMandateSigned,
                "contributions", this::checkContributions
        );
        callOnDisabled(aevt, () -> {
            Map<String, List<String>> reports = new HashMap<>();
            checkFunctions.forEach((key, value) -> {
                List<String> messages;
                try {
                    messages = value.call();
                } catch (Exception ex) { //NOPMD - Make sure the checks are not aborted.
                    LOGGER.log(Level.SEVERE, null, ex);
                    messages = Arrays.asList(ex.getLocalizedMessage().split("\n"));
                }
                reports.put(EnvironmentHandler.getResourceValue(key), messages);
            });

            Stage reportsStage = new Stage();
            reportsStage.initOwner(getStage());
            reportsStage.initModality(Modality.APPLICATION_MODAL);
            reportsStage.initStyle(StageStyle.UTILITY);
            reportsStage.setTitle(EnvironmentHandler.getResourceValue("checkData"));
            Parent checkReportDialog = CheckReportDialogUtility.createCheckReportDialog(reports);
            reportsStage.setScene(PreparationUtility.addStyle(new Scene(checkReportDialog)));
            PreparationUtility.addLogo(reportsStage)
                    .showAndWait();
        });
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateAddressesAll(ActionEvent aevt) {
        callOnDisabled(aevt, this::generateAddressesAll);
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateAddressesBirthday(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> generateAddressesBirthday(yearSpinner.getValue()));
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void generateBirthdayInfos(ActionEvent aevt) {
        if (yearSpinner.isValid()) {
            callOnDisabled(aevt, () -> {
                Integer year = yearSpinner.getValue();
                try {
                    List<Member> birthdayList = streamBirthdayMembers(year)
                            .collect(Collectors.toList());
                    if (birthdayList.isEmpty()) {
                        showNoMemberForOutputDialog();
                    } else {
                        Optional<File> path = EnvironmentHandler.askForSavePath(stage, "birthdays", "csv", year);
                        if (path.isPresent()) {
                            IOStreamUtility.printContent(
                                    BirthdayGenerator.createGroupedOutput(birthdayList, year), path.get(), true);
                        }
                    }
                } catch (IOException ex) {
                    LOGGER.log(Level.SEVERE, "Could not generate birthday infos.", ex);
                }
            });
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void showCredits() {
        String credits = EnvironmentHandler.getResourceValue("credits");
        String creditsContent = EnvironmentHandler.getResourceValue("creditsContent");
        try {
            Alert alert = DialogUtility.createMessageAlert(creditsContent, credits, credits);
            PreparationUtility.addLogo(PreparationUtility.addStyle(alert));
            Platform.runLater(alert::show);
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show credits graphically to user", ex);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void showVersion() {
        String compDateTime = AppInfo.getCompilationDate()
                .map(COMPILE_DATE_TIME_FORMATTER::format)
                .map(cpt -> "\n" + EnvironmentHandler.getResourceValue("compiledOn", cpt))
                .orElse(EnvironmentHandler.getResourceValue("unavailable"));
        String versionInfo = AppInfo.VERSION + " (" + AppInfo.UPDATE_NAME + ")" + compDateTime;
        String version = EnvironmentHandler.getResourceValue("version");
        try {
            Alert alert = DialogUtility.createInfoAlert(versionInfo, version, version);
            PreparationUtility.addLogo(PreparationUtility.addStyle(alert));
            Platform.runLater(alert::show);
        } catch (DialogCreationException ex) {
            LOGGER.log(Level.WARNING, "Could not show version information graphically to user", ex);
        }
    }

    /**
     * Returns the property containing the date when the date was last updated.
     *
     * @return The property containing the date when the date was last updated.
     * @see #getDataLastUpdated()
     */
    public ReadOnlyObjectProperty<Optional<LocalDateTime>> dataLastUpdatedProperty() {
        return dataLastUpdated;
    }

    /**
     * Returns an {@code Optional} containing the timestamp when the data was last updated.
     *
     * @return An {@code Optional} containing the timestamp when the data was last updated. Returns
     * {@code Optional.empty()} if the data is not yet received.
     */
    public Optional<LocalDateTime> getDataLastUpdated() {
        return dataLastUpdated.get();
    }

    /**
     * Returns the property holding whether all data needed by the functions of the menu is available.
     *
     * @return The property holding whether all data needed by the functions of the menu is available.
     */
    public ReadOnlyBooleanProperty allDataAvailableProperty() {
        return allDataAvailable;
    }

    /**
     * Checks whether all data needed by the functions is available.
     *
     * @return {@code true} only if all data needed by the functions of the menu is available.
     */
    public boolean isAllDataAvailable() {
        return allDataAvailable.get();
    }

    /**
     * Returns the property holding whether the currently loaded profile has activated the birthday features.
     *
     * @return The property holding whether the currently loaded profile has activated the birthday features.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u13")
    public ReadOnlyProperty<Boolean> activateBirthdayFeaturesProperty() {
        return activateBirthdayFeatures;
    }

    /**
     * Checks whether the birthday features are activated according to the currently loaded profile.
     *
     * @return {@code true} only if the birthday features are activated according to the currently loaded profile.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u13")
    public boolean isActivateBirthdayFeatures() {
        return activateBirthdayFeaturesProperty().getValue();
    }

    /**
     * Returns the property holding whether any member has associated honorings.
     *
     * @return The property holding whether any member has associated honorings.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u14")
    public BooleanBinding honoringsAvailableProperty() {
        return addedHonorings.emptyProperty().not();
    }

    /**
     * Checks whether any member has associated honorings.
     *
     * @return {@code true} only if there is at least one member which has associated honorings.
     * @deprecated The visibility of the method may be changed to package private or even to private when FXML is able
     * to access these.
     */
    @Deprecated(forRemoval = false, since = "2u14")
    public boolean isHonoringsAvailable() {
        return honoringsAvailableProperty().get();
    }

    private Stream<Member> streamCurrentMember() {
        try {
            return member.get()
                    .get()
                    .stream()
                    .filter(m -> m.getLeavingDate().isEmpty());
        } catch (InterruptedException | ExecutionException ex) {
            LOGGER.log(Level.SEVERE, "Failed to query current members. Return empty list.", ex);
            return Stream.of();
        }
    }

    private Stream<Member> streamNonContributionFree() {
        return streamCurrentMember()
                .filter(m -> !m.isContributionfree());
    }

    private Stream<Member> streamBirthdayMembers(int year) {
        return streamCurrentMember()
                .filter(m -> BirthdayGenerator.getsNotified(m, year));
    }

    @Override
    protected Optional<Void> calculateResult() {
        return Optional.empty();
    }

    /**
     * Represents a property holding a {@link CompletableFuture}. It extends {@link SimpleObjectProperty} with the
     * property of availability of the wrapped {@link CompletableFuture}.
     *
     * @param <T> The type of the result of the wrapped {@link CompletableFuture}.
     */
    private static class CompletableFutureProperty<T> extends SimpleObjectProperty<CompletableFuture<T>> {

        private static final Logger LOGGER = Logger.getLogger(CompletableFutureProperty.class.getName());
        private final BooleanProperty available = new SimpleBooleanProperty();

        /**
         * Creates a {@link CompletableFutureProperty} containing {@code null}.
         */
        CompletableFutureProperty() {
            this(null);
        }

        /**
         * Creates a {@link CompletableFutureProperty} containing the given value.
         *
         * @param initialValue The initial value.
         */
        CompletableFutureProperty(CompletableFuture<T> initialValue) {
            super(initialValue);
        }

        private void updateAvailableProperty(CompletableFuture<T> newValue) {
            available.set(false);
            CompletableFuture.runAsync(() -> {
                try {
                    newValue.get();
                    available.set(true);
                } catch (InterruptedException | ExecutionException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void set(CompletableFuture<T> newValue) {
            updateAvailableProperty(newValue);
            super.set(newValue);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void setValue(CompletableFuture<T> value) {
            updateAvailableProperty(value);
            super.setValue(value);
        }

        /**
         * This method does the same as {@link SimpleObjectProperty#get()} but throws a {@link IllegalStateException}
         * when trying to get its value but there is no requestable value (means it is {@code null}.
         *
         * @return The value hold by this property.
         * @see SimpleObjectProperty#get()
         */
        @Override
        public CompletableFuture<T> get() {
            CompletableFuture<T> value = super.get();
            if (value == null) {
                throw new IllegalStateException("There is no data queried yet.\n"
                        + "You have to set a connection first before being able to operate on that data.");
            }
            return value;
        }

        /**
         * Returns the property holding whether the data of the contained {@link Future} object is available.
         *
         * @return The property holding whether the data of the contained {@link Future} object is available.
         */
        public ReadOnlyBooleanProperty availableProperty() {
            return available;
        }

        /**
         * Checks whether the data of the contained {@link Future} object is available.
         *
         * @return {@code true} only if the data of the contained {@link Future} object is available.
         */
        public boolean isAvailable() {
            return available.get();
        }
    }
}
