package bayern.steinbrecher.gruen2.sepa;

import bayern.steinbrecher.gruen2.sepa.form.SepaModel;
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
 * @author Stefan Huber
 */
public class SepaPain00800302_XML_Generator {

    @SuppressWarnings("FieldMayBeFinal")
    private static final SimpleDateFormat yearMonthDayHourMinuteSecondFormat = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ss");

    /**
     * Schreibt die SepaModel-XML-Datei f&uuml;r alle beitragspflichtigen
     * Mitglieder des GTEV Traunwalchen mit den gegebenen Daten
     *
     * @param member Die Mitglieder, von denen abgebucht werden soll.
     * @param contribution Der zu zahlende Beitrag
     * @param eigeninfo Das Objekt, das die SepaModel-Eigeninfos enth&auml;lt
     * @param outputfile Der Name der Datei f&uuml;r den XML-Quellcode
     */
    public static void createXMLFile(LinkedList<Member> member, double contribution, OriginatorInfo eigeninfo, String outputfile) {
        filterValidMember(member);
        int numberOfTransactions = member.size();
        double controlSum = numberOfTransactions * contribution;
        createXMLFile(createXML(member, eigeninfo, numberOfTransactions, contribution, controlSum), outputfile);
    }

    /**
     * Schreibt die SepaModel-XML-Datei names <code>filename</code>
     *
     * @param output Der Inhalt f&uuml;r die SepaModel-Datei
     * @param filename Der Name der Datei f&uuml;r den XML-Quellcode
     */
    private static void createXMLFile(StringBuilder output, String filename) {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename), "UTF-8"))) {
            bw.append('\uFEFF') //Dies dient dazu aus "UTF-8 ohne Bom", "UTF-8 MIT Bom" zu machen, damit Sonderzeichen korrekt interpretiert werden.
                    .append(output);
        } catch (IOException ex) {
            Logger.getLogger(SepaModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gibt das heutige Datum in yyyy-MM-ddThh:mm:ss formatiert zur&uuml;ck
     *
     * @return Das heutige Datum als String formatiert
     */
    private static String getFormatedDateTimeNow() {
        Date today = Calendar.getInstance().getTime();
        return yearMonthDayHourMinuteSecondFormat.format(today);
    }

    /**
     * Liest die Datei Eigeninfos.txt ein und erstellt den Quelltext f&uuml;r
     * alle Mitglieder im gegebenen ResultSet.
     *
     * @param members Die Mitglieder, denen der Beitrag abgebucht werden soll.
     * (Zeile der Spalten&uuml;berschriften vorher entfernen!)
     * @param indizes Die HashMap, die den Spaltennamen Spaltenindizes in
     * <code>members</code> zuordnet.
     * @return Eine StringBuilder-Repr&auml;sentation der XML-Datei.
     */
    private static StringBuilder createXML(LinkedList<Member> member, OriginatorInfo eigeninfo, int numberOfTransactions, double contribution, double controlSum) {
        StringBuilder output = new StringBuilder();

        //Der Beginn mit unseren Daten
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02 pain.008.003.02.xsd\">\n")
                .append(" <CstmrDrctDbtInitn>\n")
                .append("   <GrpHdr>\n")
                .append("     <MsgId>")
                .append(eigeninfo.getMsgId())
                .append("</MsgId>\n")
                .append("     <CreDtTm>")
                .append(getFormatedDateTimeNow())
                .append("</CreDtTm>\n")
                .append("     <NbOfTxs>")
                .append(numberOfTransactions)
                .append("</NbOfTxs>\n")
                .append("     <InitgPty>\n")
                .append("       <Nm>")
                .append(eigeninfo.getCreator())
                .append("</Nm>\n")
                .append("     </InitgPty>\n")
                .append("   </GrpHdr>\n")
                .append("   <PmtInf>\n")
                .append("     <PmtInfId>")
                .append(eigeninfo.getPmtInfId())
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
                .append(eigeninfo.getExecutiondate())
                .append("</ReqdColltnDt>\n")
                .append("     <Cdtr>\n")
                .append("       <Nm>")
                .append(eigeninfo.getCreditor())
                .append("</Nm>\n")
                .append("     </Cdtr>\n")
                .append("     <CdtrAcct>\n")
                .append("       <Id>\n")
                .append("         <IBAN>")
                .append(eigeninfo.getIban())
                .append("</IBAN>\n")
                .append("       </Id>\n")
                .append("     </CdtrAcct>\n")
                .append("     <CdtrAgt>\n")
                .append("       <FinInstnId>\n")
                .append("         <BIC>")
                .append(eigeninfo.getBic())
                .append("</BIC>\n")
                .append("       </FinInstnId>\n")
                .append("     </CdtrAgt>\n")
                .append("     <ChrgBr>SLEV</ChrgBr>\n")
                .append("     <CdtrSchmeId>\n")
                .append("       <Id>\n")
                .append("         <PrvtId>\n")
                .append("           <Othr>\n")
                .append("             <Id>")
                .append(eigeninfo.getTrusterId())
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
                    .append(eigeninfo.getPurpose())
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

    private static void filterValidMember(LinkedList<Member> member) {
        LinkedList<Member> invalidMember = new LinkedList<>();
        member.forEach(m -> {
            boolean valid = true;
            if (!m.hasIban()) {
                valid = false;
                System.err.println(m.getVorname() + m.getNachname() + " hat keine IBAN");
            }
            if (!m.hasBic()) {
                valid = false;
                System.err.println(m.getVorname() + m.getNachname() + " hat keine BIC");
            }
            if (!valid) {
                invalidMember.add(m);
            }
        });
        member.removeAll(invalidMember);
    }
}
