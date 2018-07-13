/*
 * Copyright (C) 2018 Stefan Huber
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bayern.steinbrecher.green2.generator.sepa;

import bayern.steinbrecher.green2.people.AccountHolder;
import bayern.steinbrecher.green2.people.Member;
import bayern.steinbrecher.green2.people.Originator;
import bayern.steinbrecher.green2.utility.IOStreamUtility;
import bayern.steinbrecher.green2.utility.SepaUtility;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/**
 * Generates and checks a Sepa.Pain.008.003.02 file.
 *
 * @author Stefan Huber
 */
public final class SepaPain00800302XMLGenerator {

    /**
     * Prohibit construction of an object.
     */
    private SepaPain00800302XMLGenerator() {
        throw new UnsupportedOperationException("Construction of an object not supported.");
    }

    /**
     * Generates a xml-file containing all valid member of {@code member} and prints the generated output into
     * {@code outputfile} if there are any.If {@code outputfile} already exists it will be replaced on generation. If it
     * don´t it will be created.
     *
     * @param member The member to collect money via direct debit from.
     * @param originator The originator of the direct debit.
     * @param sequenceType The sequence type of the direct debit.
     * @param outputfile The path to the file to print the xml to.
     * @param sepaWithBom Indicates whether to use UTF-8 with or without BOM.
     * @return A list containing member which are not included in the outputfile. These are member which have no iban or
     * no bic.
     * @throws java.io.IOException Thrown if any I/O error occurs.
     */
    public static List<Member> createXMLFile(Collection<Member> member, Originator originator,
            SequenceType sequenceType, File outputfile, boolean sepaWithBom) throws IOException {
        List<Member> invalidMember = filterValidMember(member);
        if (member.isEmpty()) {
            Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                    .log(Level.WARNING, "There are no valid members left to generate output for.");
        } else {
            IOStreamUtility.printContent(createXML(member, originator, sequenceType), outputfile, sepaWithBom);
        }
        return invalidMember;
    }

    /**
     * Removes all member which have no iban or no bic from {@link Member}.
     *
     * @param member The list of member to filter.
     * @return The list containing the member EXCLUDED from {@link Member}.
     */
    private static List<Member> filterValidMember(Collection<Member> member) {
        List<Member> invalidMember = new LinkedList<>();
        member.parallelStream().forEach(m -> {
            boolean valid = true;
            AccountHolder accountHolder = m.getAccountHolder();
            if (!(accountHolder.hasIban() && SepaUtility.isValidIban(accountHolder.getIban()))) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has an invalid IBAN", m);
            }
            if (!accountHolder.hasBic()) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName()).log(Level.WARNING, "{0} has no BIC", m);
            }
            if (accountHolder.getMandateSigned() == null) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has a bad \"MandatErstellt\"", m);
            }
            if (m.getContribution().isPresent()) {
                if (m.getContribution().get() <= 0) {
                    valid = false;
                    Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                            .log(Level.WARNING, "{0} has a contribution <= 0.", m);
                }
            } else {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has no assinged contribution.", m);
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
     * @param originator The originator of the direct debit.
     * @param contributions The mapping of membershipnumbers to contributions.
     * @return The {@link String} representing the xml file content.
     */
    @SuppressWarnings({"PMD.ExcessiveMethodLength", "PMD.ConsecutiveLiteralAppends"})
    private static String createXML(Collection<Member> member, Originator originator, SequenceType sequenceType) {
        int numberOfTransactions = member.size();
        double controlSum = member.parallelStream()
                .mapToDouble(m -> m.getContribution().get())
                .sum();
        //CHECKSTYLE.OFF: MagicNumber - Eliminate double precision inaccuracy arose from IntStream
        controlSum = Math.rint(controlSum * 100) / 100;
        //CHECKSTYLE.ON: MagicNumber
        StringBuilder output = new StringBuilder(2048);

        //The beginning containing originators data.
        String sepaDateToday = SepaUtility.getSepaDate(LocalDateTime.now());
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
                .append("<Document xmlns=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02\" ")
                .append("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" ")
                .append("xsi:schemaLocation=\"urn:iso:std:iso:20022:tech:xsd:pain.008.003.02 pain.008.003.02.xsd\">\n")
                .append("    <CstmrDrctDbtInitn>\n")
                .append("        <GrpHdr>\n")
                .append("            <MsgId>").append(originator.getMsgId()).append("</MsgId>\n")
                .append("            <CreDtTm>").append(sepaDateToday).append("</CreDtTm>\n")
                .append("            <NbOfTxs>").append(numberOfTransactions).append("</NbOfTxs>\n")
                .append("            <InitgPty>\n")
                .append("                <Nm>").append(originator.getCreator()).append("</Nm>\n")
                .append("            </InitgPty>\n")
                .append("        </GrpHdr>\n")
                .append("        <PmtInf>\n")
                .append("            <PmtInfId>").append(originator.getPmtInfId()).append("</PmtInfId>\n")
                .append("            <PmtMtd>DD</PmtMtd>\n")
                .append("            <BtchBookg>true</BtchBookg>\n")
                .append("            <NbOfTxs>").append(numberOfTransactions).append("</NbOfTxs>\n")
                .append("            <CtrlSum>").append(controlSum).append("</CtrlSum>\n")
                .append("            <PmtTpInf>\n")
                .append("                <SvcLvl>\n")
                .append("                    <Cd>SEPA</Cd>\n")
                .append("                </SvcLvl>\n")
                .append("                <LclInstrm>\n")
                .append("                    <Cd>CORE</Cd>\n")
                .append("                </LclInstrm>\n")
                .append("                <SeqTp>").append(sequenceType).append("</SeqTp>\n")
                .append("            </PmtTpInf>\n")
                .append("            <ReqdColltnDt>").append(originator.getExecutionDate()).append("</ReqdColltnDt>\n")
                .append("            <Cdtr>\n")
                .append("                <Nm>").append(originator.getCreditor()).append("</Nm>\n")
                .append("            </Cdtr>\n")
                .append("            <CdtrAcct>\n")
                .append("                <Id>\n")
                .append("                    <IBAN>").append(originator.getIban()).append("</IBAN>\n")
                .append("                </Id>\n")
                .append("            </CdtrAcct>\n")
                .append("            <CdtrAgt>\n")
                .append("                <FinInstnId>\n")
                .append("                    <BIC>").append(originator.getBic()).append("</BIC>\n")
                .append("                </FinInstnId>\n")
                .append("            </CdtrAgt>\n")
                .append("            <ChrgBr>SLEV</ChrgBr>\n")
                .append("            <CdtrSchmeId>\n")
                .append("                <Id>\n")
                .append("                    <PrvtId>\n")
                .append("                        <Othr>\n")
                .append("                            <Id>").append(originator.getCreditorId()).append("</Id>\n")
                .append("                            <SchmeNm>\n")
                .append("                                <Prtry>SEPA</Prtry>\n")
                .append("                            </SchmeNm>\n")
                .append("                        </Othr>\n")
                .append("                    </PrvtId>\n")
                .append("                </Id>\n")
                .append("            </CdtrSchmeId>\n");

        //The member.
        member.parallelStream().forEach(m -> {
            AccountHolder accountHolder = m.getAccountHolder();
            Double contribution = m.getContribution().get();
            String mandatSigned = SepaUtility.getSepaDate(accountHolder.getMandateSigned());
            StringBuilder suboutput = new StringBuilder()
                    .append("            <DrctDbtTxInf>\n")
                    .append("                <PmtId>\n")
                    .append("                    <EndToEndId>NOTPROVIDED</EndToEndId>\n")
                    .append("                </PmtId>\n")
                    .append("                <InstdAmt Ccy=\"EUR\">").append(contribution).append("</InstdAmt>\n")
                    .append("                <DrctDbtTx>\n")
                    .append("                    <MndtRltdInf>\n")
                    .append("                        <MndtId>").append(m.getMembershipnumber()).append("</MndtId>\n")
                    .append("                        <DtOfSgntr>").append(mandatSigned).append("</DtOfSgntr>\n")
                    .append("                        <AmdmntInd>").append(accountHolder.hasMandateChanged())
                    .append("</AmdmntInd>\n")
                    .append("                    </MndtRltdInf>\n")
                    .append("                </DrctDbtTx>\n")
                    .append("                <DbtrAgt>\n")
                    .append("                    <FinInstnId>\n")
                    .append("                        <BIC>").append(accountHolder.getBic()).append("</BIC>\n")
                    .append("                    </FinInstnId>\n")
                    .append("                </DbtrAgt>\n")
                    .append("                <Dbtr>\n")
                    .append("                    <Nm>").append(m.getAccountHolderName()).append("</Nm>\n")
                    .append("                </Dbtr>\n")
                    .append("                <DbtrAcct>\n")
                    .append("                    <Id>\n")
                    .append("                        <IBAN>").append(accountHolder.getIban()).append("</IBAN>\n")
                    .append("                    </Id>\n")
                    .append("                </DbtrAcct>\n")
                    .append("                <RmtInf>\n")
                    .append("                    <Ustrd>").append(originator.getPurpose()).append("</Ustrd>\n")
                    .append("                </RmtInf>\n")
                    .append("            </DrctDbtTxInf>\n");

            synchronized (output) {
                output.append(suboutput);
            }
        });

        //The last closing tags.
        output.append("        </PmtInf>\n")
                .append("    </CstmrDrctDbtInitn>\n")
                .append("</Document>");

        String xmlOutput = output.toString();
        Optional<String> errorMessage;
        try {
            errorMessage = SepaUtility.validateSepaXML(xmlOutput);
        } catch (SAXException | IOException ex) {
            throw new AssertionError("The validation of the generated SEPA xml output failed.", ex);
        }
        if (errorMessage.isPresent()) {
            throw new AssertionError("Erroneous SEPA xml output was generated:\n" + errorMessage.get());
        } else {
            return xmlOutput;
        }
    }
}
