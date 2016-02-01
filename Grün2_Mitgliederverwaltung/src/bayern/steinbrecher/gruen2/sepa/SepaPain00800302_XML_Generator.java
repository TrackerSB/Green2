package bayern.steinbrecher.gruen2.sepa;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a Sepa.Pain.008.003.02.
 *
 * @author Stefan Huber
 */
public class SepaPain00800302_XML_Generator {

    private static final SimpleDateFormat YEAR_MONT_DAY_T_HOUR_MINUTE_SECOND
            = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

    private SepaPain00800302_XML_Generator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported.");
    }

    /**
     * Schreibt die SepaModel-XML-Datei f&uuml;r alle beitragspflichtigen
     * Mitglieder des GTEV Traunwalchen mit den gegebenen Daten.
     *
     * @param member Die Mitglieder, von denen abgebucht werden soll.
     * @param contribution Der zu zahlende Beitrag
     * @param originator Das Objekt, das die SepaModel-Eigeninfos enth&auml;lt
     * @param outputfile Der Name der Datei f&uuml;r den XML-Quellcode
     */
    public static void createXMLFile(LinkedList<Member> member,
            double contribution, Originator originator, String outputfile) {
        filterValidMember(member);
        int numberOfTransactions = member.size();
        double controlSum = numberOfTransactions * contribution;
        createXMLFile(createXML(member, originator, numberOfTransactions,
                contribution, controlSum), outputfile);
    }

    private static void filterValidMember(LinkedList<Member> member) {
        LinkedList<Member> invalidMember = new LinkedList<>();
        member.forEach(m -> {
            boolean valid = true;
            if (!m.hasIban()) {
                valid = false;
                System.err.println(m.getVorname() + m.getNachname()
                        + " has no IBAN");
            }
            if (!m.hasBic()) {
                valid = false;
                System.err.println(m.getVorname() + m.getNachname()
                        + " has no BIC");
            }
            if (!valid) {
                invalidMember.add(m);
            }
        });
        member.removeAll(invalidMember);
    }

    /**
     * Schreibt die SepaModel-XML-Datei names <code>filename</code>.
     *
     * @param output Der Inhalt f&uuml;r die SepaModel-Datei
     * @param filename Der Name der Datei f&uuml;r den XML-Quellcode
     */
    private static void createXMLFile(StringBuilder output, String filename) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filename), "UTF-8"))) {
            /*
             * Dies dient dazu aus "UTF-8 ohne Bom", "UTF-8 MIT Bom" zu machen,
             * damit Sonderzeichen korrekt interpretiert werden.
             */
            bw.append('\uFEFF')
                    .append(output);
        } catch (IOException ex) {
            Logger.getLogger(SepaPain00800302_XML_Generator.class.getName())
                    .log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Liest die Datei Eigeninfos.txt ein und erstellt den Quelltext f&uuml;r
     * alle Mitglieder im gegebenen ResultSet.
     *
     * @param member Die Mitglieder, denen der Beitrag abgebucht werden soll.
     * (Zeile der Spalten&uuml;berschriften vorher entfernen!)
     * @return Eine StringBuilder-Repr&auml;sentation der XML-Datei.
     */
    private static StringBuilder createXML(LinkedList<Member> member,
            Originator originator, int numberOfTransactions,
            double contribution, double controlSum) {
        StringBuilder output = new StringBuilder();

        //Der Beginn mit unseren Daten
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02 pain.008.003.02.xsd\">\n")
                .append(" <CstmrDrctDbtInitn>\n")
                .append("   <GrpHdr>\n")
                .append("     <MsgId>")
                .append(originator.getMsgId())
                .append("</MsgId>\n")
                .append("     <CreDtTm>")
                .append(getFormatedDateTimeNow())
                .append("</CreDtTm>\n")
                .append("     <NbOfTxs>")
                .append(numberOfTransactions)
                .append("</NbOfTxs>\n")
                .append("     <InitgPty>\n")
                .append("       <Nm>")
                .append(originator.getCreator())
                .append("</Nm>\n")
                .append("     </InitgPty>\n")
                .append("   </GrpHdr>\n")
                .append("   <PmtInf>\n")
                .append("     <PmtInfId>")
                .append(originator.getPmtInfId())
                .append("</PmtInfId>\n")
                .append("     <PmtMtd>DD</PmtMtd>\n")
                .append("     <BtchBookg>true</BtchBookg>\n")
                .append("     <NbOfTxs>")
                .append(numberOfTransactions)
                .append("</NbOfTxs>\n")
                .append("     <CtrlSum>")
                .append(controlSum)
                .append("</CtrlSum>\n")
                .append("     <PmtTpInf>\n")
                .append("       <SvcLvl>\n")
                .append("         <Cd>SEPA</Cd>\n")
                .append("       </SvcLvl>\n")
                .append("       <LclInstrm>\n")
                .append("         <Cd>CORE</Cd>\n")
                .append("       </LclInstrm>\n")
                .append("       <SeqTp>RCUR</SeqTp>\n")
                .append("     </PmtTpInf>\n")
                .append("     <ReqdColltnDt>")
                .append(originator.getExecutiondate())
                .append("</ReqdColltnDt>\n")
                .append("     <Cdtr>\n")
                .append("       <Nm>")
                .append(originator.getCreditor())
                .append("</Nm>\n")
                .append("     </Cdtr>\n")
                .append("     <CdtrAcct>\n")
                .append("       <Id>\n")
                .append("         <IBAN>")
                .append(originator.getIban())
                .append("</IBAN>\n")
                .append("       </Id>\n")
                .append("     </CdtrAcct>\n")
                .append("     <CdtrAgt>\n")
                .append("       <FinInstnId>\n")
                .append("         <BIC>")
                .append(originator.getBic())
                .append("</BIC>\n")
                .append("       </FinInstnId>\n")
                .append("     </CdtrAgt>\n")
                .append("     <ChrgBr>SLEV</ChrgBr>\n")
                .append("     <CdtrSchmeId>\n")
                .append("       <Id>\n")
                .append("         <PrvtId>\n")
                .append("           <Othr>\n")
                .append("             <Id>")
                .append(originator.getTrusterId())
                .append("</Id>\n")
                .append("             <SchmeNm>\n")
                .append("               <Prtry>SEPA</Prtry>\n")
                .append("             </SchmeNm>\n")
                .append("           </Othr>\n")
                .append("         </PrvtId>\n")
                .append("       </Id>\n")
                .append("     </CdtrSchmeId>\n");

        /*
         * Die Mitglieder
         */
        member.parallelStream().forEach(m -> {
            StringBuilder suboutput = new StringBuilder("     <DrctDbtTxInf>\n")
                    .append("       <PmtId>\n")
                    .append("         <EndToEndId>NOTPROVIDED</EndToEndId>\n")
                    .append("       </PmtId>\n")
                    .append("       <InstdAmt Ccy=\"EUR\">")
                    .append(contribution)
                    .append("</InstdAmt>\n")
                    .append("       <DrctDbtTx>\n")
                    .append("         <MndtRltdInf>\n")
                    .append("           <MndtId>")
                    .append(m.getMitgliedsnummer())
                    .append("</MndtId>\n")
                    .append("           <DtOfSgntr>")
                    .append(m.getMandatErstellt())
                    .append("</DtOfSgntr>\n")
                    .append("           <AmdmntInd>")
                    .append(m.hasMandatChanged())
                    .append("</AmdmntInd>\n")
                    .append("         </MndtRltdInf>\n")
                    .append("       </DrctDbtTx>\n")
                    .append("       <DbtrAgt>\n")
                    .append("         <FinInstnId>\n")
                    .append("           <BIC>")
                    .append(m.getBic())
                    .append("</BIC>\n")
                    .append("         </FinInstnId>\n")
                    .append("       </DbtrAgt>\n")
                    .append("       <Dbtr>\n")
                    .append("         <Nm>")
                    .append(m.getNachname())
                    .append(", ")
                    .append(m.getVorname())
                    .append("</Nm>\n")
                    .append("       </Dbtr>\n")
                    .append("       <DbtrAcct>\n")
                    .append("         <Id>\n")
                    .append("           <IBAN>")
                    .append(m.getIban())
                    .append("</IBAN>\n")
                    .append("         </Id>\n")
                    .append("       </DbtrAcct>\n")
                    .append("       <RmtInf>\n")
                    .append("         <Ustrd>")
                    .append(originator.getPurpose())
                    .append("</Ustrd>\n")
                    .append("       </RmtInf>\n")
                    .append("     </DrctDbtTxInf>\n");
            synchronized (output) {
                output.append(suboutput);
            }
        });

        //Der Schluss
        output.append("   </PmtInf>\n")
                .append(" </CstmrDrctDbtInitn>\n")
                .append("</Document>");

        return output;
    }

    /**
     * Gibt das heutige Datum in yyyy-MM-ddThh:mm:ss formatiert zur&uuml;ck.
     *
     * @return Das heutige Datum als String formatiert
     */
    private static String getFormatedDateTimeNow() {
        Date today = Calendar.getInstance().getTime();
        return YEAR_MONT_DAY_T_HOUR_MINUTE_SECOND.format(today);
    }
}
