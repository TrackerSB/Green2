package de.traunviertler_traunwalchen.stefanhuber.memberselectiondialog;

import de.traunviertler_traunwalchen.stefanhuber.dataprovider.DataProvider;
import de.traunviertler_traunwalchen.stefanhuber.sepa.Eigeninfo;
import de.traunviertler_traunwalchen.stefanhuber.sepa.Member;
import de.traunviertler_traunwalchen.stefanhuber.sepadialog.SepaModel;
import de.traunviertler_traunwalchen.stefanhuber.sepa.SepaPain00800302_XML_Generator;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author Stefan Huber
 */
public class MemberSelectionModel extends Application {

    private HashMap<Member, Boolean> allMember;
    private MemberSelectionController memberSelecotionController;
    private Eigeninfo eigeninfo;
    private double contribution;
    private Callable callable = () -> {
        SepaPain00800302_XML_Generator.createXMLFile(getSelectedMember(), contribution, eigeninfo, DataProvider.getSavepath() + "/Sepa.xml");
        return null;
    };

    public MemberSelectionModel(LinkedList<Member> allMember, Eigeninfo eigeninfo) {
        this.allMember = new HashMap<>();
        allMember.forEach(m -> {
            this.allMember.put(m, false);
        });
        this.eigeninfo = eigeninfo;
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("MemberSelection.fxml"));
            Parent root = fxmlLoader.load();
            root.getStylesheets().add(DataProvider.getStylesheetPath());
            memberSelecotionController = fxmlLoader.getController();
            memberSelecotionController.setModel(this);

            Scene scene = new Scene(root);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Abzubuchende Konten wählen");
            primaryStage.setResizable(false);
            primaryStage.getIcons().add(DataProvider.getIcon());
            primaryStage.show();
        } catch (IOException ex) {
            Logger.getLogger(MemberSelectionModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void callCallable() {
        try {
            callable.call();
        } catch (Exception ex) {
            Logger.getLogger(SepaModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void setContribution(double contribution) {
        this.contribution = contribution;
    }

    public LinkedList<Member> getSelectedMember() {
        LinkedList<Member> selectedMember = new LinkedList<>();
        allMember.forEach((m, b) -> {
            if (b) {
                selectedMember.add(m);
            }
        });
        return selectedMember;
    }

    public Set<Member> getAllMember() {
        return allMember.keySet();
    }

    public void setSelectedMember(Member m) {
        allMember.put(m, true);
    }

    public void setUnselectedMember(Member m) {
        allMember.put(m, false);
    }
}
