package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.Output;
import bayern.steinbrecher.gruen2.member.AccountHolder;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import bayern.steinbrecher.gruen2.sepa.Originator;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates a Sepa.Pain.008.003.02.
 *
 * @author Stefan Huber
 */
public class SepaPain00800302_XML_Generator {

    /**
     * Prohibit construction of an object.
     */
    private SepaPain00800302_XML_Generator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported.");
    }

    /**
     * Generates a xml-file containing all member of {@code member}, which have
     * a iban and a bic, and prints the generated output into
     * {@code outputfile}. If {@code outputfile} already exists it will be
     * replaced. If it don´t it will be created.
     *
     * @param member The member to collect money via direct debit from.
     * @param contribution The contribution every member has to pay.
     * @param originator The originator of the direct debit.
     * @param outputfile The path to the file to print the xml to.
     * @return A list containing member which are not included in the
     * outputfile. These are member which have no iban or no bic.
     */
    public static List<Member> createXMLFile(List<Member> member,
            double contribution, Originator originator, String outputfile) {
        List<Member> invalidMember = filterValidMember(member);
        Output.printContent(
                createXML(member, originator, contribution), outputfile);
        return invalidMember;
    }

    /**
     * Removes all member which have no iban or no bic from {@code member}.
     *
     * @param member The list of member to filter.
     * @return The list containing the member EXCLUDED from {@code member}.
     */
    private static List<Member> filterValidMember(List<Member> member) {
        List<Member> invalidMember = new LinkedList<>();
        member.parallelStream().forEach(m -> {
            boolean valid = true;
            Person p = m.getPerson();
            AccountHolder ah = m.getAccountHolder();
            if (!ah.hasIban()) {
                valid = false;
                System.err.println(p.getPrename() + p.getLastname()
                        + " has no IBAN");
            }
            if (!ah.hasBic()) {
                valid = false;
                System.err.println(p.getPrename() + p.getLastname()
                        + " has no BIC");
            }
            if (!valid) {
                invalidMember.add(m);
            }
        });
        member.removeAll(invalidMember);
        return invalidMember;
    }

    /**
     * Generates the output for the sepa-xml-file.
     *
     * @param member The list of member to include in the xml.
     * @param originator The origiantor of the direct debit.
     * @param contribution The amount every member has to pay.
     * @return The {@code String} representing the xml file content.
     */
    private static String createXML(List<Member> member, Originator originator,
            double contribution) {
        int numberOfTransactions = member.size();
        double controlSum = numberOfTransactions * contribution;
        StringBuilder output = new StringBuilder();

        //The beginning containing originators data.
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:")
                .append("pain.008.003.02\" xmlns:xsi")
                .append("=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:")
                .append("pain.008.003.02 pain.008.003.02.xsd\">\n")
                .append(" <CstmrDrctDbtInitn>\n")
                .append("   <GrpHdr>\n")
                .append("     <MsgId>")
                .append(originator.getMsgId())
                .append("</MsgId>\n")
                .append("     <CreDtTm>")
                .append(LocalDate.now())
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

        //The member.
        member.parallelStream().forEach(m -> {
            AccountHolder ah = m.getAccountHolder();
            StringBuilder suboutput = new StringBuilder()
                    .append("     <DrctDbtTxInf>\n")
                    .append("       <PmtId>\n")
                    .append("         <EndToEndId>NOTPROVIDED</EndToEndId>\n")
                    .append("       </PmtId>\n")
                    .append("       <InstdAmt Ccy=\"EUR\">")
                    .append(contribution)
                    .append("</InstdAmt>\n")
                    .append("       <DrctDbtTx>\n")
                    .append("         <MndtRltdInf>\n")
                    .append("           <MndtId>")
                    .append(m.getMembershipnumber())
                    .append("</MndtId>\n")
                    .append("           <DtOfSgntr>")
                    .append(ah.getMandatSigned())
                    .append("</DtOfSgntr>\n")
                    .append("           <AmdmntInd>")
                    .append(ah.hasMandatChanged())
                    .append("</AmdmntInd>\n")
                    .append("         </MndtRltdInf>\n")
                    .append("       </DrctDbtTx>\n")
                    .append("       <DbtrAgt>\n")
                    .append("         <FinInstnId>\n")
                    .append("           <BIC>")
                    .append(ah.getBic())
                    .append("</BIC>\n")
                    .append("         </FinInstnId>\n")
                    .append("       </DbtrAgt>\n")
                    .append("       <Dbtr>\n")
                    .append("         <Nm>")
                    .append(ah.getLastname())
                    .append(", ")
                    .append(ah.getPrename())
                    .append("</Nm>\n")
                    .append("       </Dbtr>\n")
                    .append("       <DbtrAcct>\n")
                    .append("         <Id>\n")
                    .append("           <IBAN>")
                    .append(ah.getIban())
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

        //The last closing tags.
        output.append("   </PmtInf>\n")
                .append(" </CstmrDrctDbtInitn>\n")
                .append("</Document>");

        return output.toString();
    }
}
