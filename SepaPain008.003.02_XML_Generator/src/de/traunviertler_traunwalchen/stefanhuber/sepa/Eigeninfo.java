package de.traunviertler_traunwalchen.stefanhuber.sepa;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Die Objekte dieser Klasse repr&auml;sentieren die Informationen &uuml;ber den
 * Sammler der Sepa-Lastschrift.
 *
 * @author Stefan Huber
 */
public class Eigeninfo {

    private String creator,
            msgId,
            creditor,
            iban,
            bic,
            trusterId,
            pmtInfId,
            executiondate,
            purpose,
            filename;

    /**
     * Erstellt ein Objekt, das die Sepa-Eigeninfos der Datei
     * <code>filename</code> repr&auml;sentiert.
     *
     * @param filename Der Name der Eigeninfos-Datei
     */
    public Eigeninfo(String filename) {
        this.filename = filename;
        creator = msgId = creditor = iban = bic = trusterId = pmtInfId = executiondate = purpose = "";
    }

    /**
     * Erstellt ein Eigeninfo-Objekt und liest die Datei <code>filename</code>
     * ein, die Informationen &uuml;ber den Ersteller des Einzuges enth&auml;lt,
     * falls die Datei existiert.
     *
     * @param filename Die einzulesende Datei
     * @return Neues Eigeninfo-Objekt, das <code>filename</code> bereits
     * eingelesen hat.
     * @throws java.io.FileNotFoundException Tritt auf, wenn die Datei nicht
     * gefunden werden konnte.
     */
    public static Eigeninfo readInEigeninfosTxt(String filename) throws FileNotFoundException {
        Eigeninfo e = new Eigeninfo(filename);
        e.readInEigeninfosTxt();
        return e;
    }

    /**
     * Liest die Datei, die den Eigeninfo-Objekt bei der Erzeugung
     * &uuml;bergeben wurde ein, falls die Datei existiert.
     *
     * @throws FileNotFoundException Tritt auf, wenn die Datei nicht gefunden
     * werden konnte.
     */
    public void readInEigeninfosTxt() throws FileNotFoundException {
        try (Scanner sc = new Scanner(new File(filename))) {
            this.creator = sc.nextLine();
            this.creditor = sc.nextLine();
            this.iban = sc.nextLine();
            this.bic = sc.nextLine();
            this.purpose = sc.nextLine();
            this.trusterId = sc.nextLine();
            this.msgId = sc.nextLine();
            this.executiondate = sc.nextLine();
            this.pmtInfId = sc.nextLine();
        } catch (NoSuchElementException ex) {
            Logger.getLogger(Eigeninfo.class.getName()).log(Level.SEVERE, filename + " hat zu wenig Zeilen", ex);
        }
    }

    /**
     * Schreibt die eingegebenen Daten in Eigeninfos.txt. Falls die Datei noch
     * nicht existiert, wird sie erstellt.
     */
    public void updateEigeninfosTxt() {
        try (PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(filename)))) {
            pw.println(creator);
            pw.println(creditor);
            pw.println(iban);
            pw.println(bic);
            pw.println(purpose);
            pw.println(trusterId);
            pw.println(msgId);
            pw.println(executiondate);
            pw.println(pmtInfId);
        } catch (IOException ex) {
            Logger.getLogger(Eigeninfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getCreditor() {
        return creditor;
    }

    public void setCreditor(String creditor) {
        this.creditor = creditor;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getTrusterId() {
        return trusterId;
    }

    public void setTrusterId(String trusterId) {
        this.trusterId = trusterId;
    }

    public String getPmtInfId() {
        return pmtInfId;
    }

    public void setPmtInfId(String pmtInfId) {
        this.pmtInfId = pmtInfId;
    }

    public String getExecutiondate() {
        return executiondate;
    }

    public void setExecutiondate(String executiondate) {
        this.executiondate = executiondate;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
