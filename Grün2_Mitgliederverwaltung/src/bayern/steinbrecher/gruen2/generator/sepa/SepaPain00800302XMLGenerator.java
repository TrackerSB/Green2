/*
 * Copyright (C) 2016 Stefan Huber
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
package bayern.steinbrecher.gruen2.generator.sepa;

import bayern.steinbrecher.gruen2.utility.IOStreamUtility;
import bayern.steinbrecher.gruen2.people.AccountHolder;
import bayern.steinbrecher.gruen2.people.Member;
import bayern.steinbrecher.gruen2.people.Person;
import bayern.steinbrecher.gruen2.people.Originator;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a Sepa.Pain.008.003.02.
 *
 * @author Stefan Huber
 */
public final class SepaPain00800302XMLGenerator {

    private static final SimpleDateFormat SDF
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    /**
     * Length of coutrycoude (CC);
     */
    private static final int SEPA_CC_LENGTH = 2;
    /**
     * Length of coutrycoude (CC) and checksum together.
     */
    private static final int SEPA_CC_CHECKSUM_LENGTH = SEPA_CC_LENGTH + 2;
    private static final int SEPA_MIN_LENGT = SEPA_CC_CHECKSUM_LENGTH + 1;
    private static final int SEPA_SHIFT_ASCII_ALPHABET = 55;
    /**
     * A=10, B=11, C=12 etc.
     */
    private static final int SEPA_SHIFT_COUTRYCODE = 10;
    private static final int SEPA_CHECKSUM_MODULO = 97;

    /**
     * Prohibit construction of an object.
     */
    private SepaPain00800302XMLGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object not supported.");
    }

    /**
     * Checks whether the IBAN of the given member has a valid checksum.
     *
     * @param ah The account holder whose IBAN to check.
     * @return {@code true }, only if members IBAN has a valid checksum.
     */
    public static boolean hasValidIban(AccountHolder ah) {
        if (!ah.hasIban()) {
            return false;
        }

        String iban = ah.getIban();
        int posAlphabetFirstChar
                = ((int) iban.charAt(0)) - SEPA_SHIFT_ASCII_ALPHABET;
        int posAlphabetSecondChar
                = ((int) iban.charAt(1)) - SEPA_SHIFT_ASCII_ALPHABET;
        if (iban.length() < SEPA_MIN_LENGT
                || posAlphabetFirstChar < SEPA_SHIFT_COUTRYCODE
                || posAlphabetSecondChar < SEPA_SHIFT_COUTRYCODE) {
            return false;
        }

        iban = iban.substring(SEPA_CC_CHECKSUM_LENGTH) + posAlphabetFirstChar
                + posAlphabetSecondChar
                + iban.substring(2, SEPA_CC_CHECKSUM_LENGTH);
        return new BigInteger(iban).mod(
                BigInteger.valueOf(SEPA_CHECKSUM_MODULO))
                .equals(BigInteger.ONE);
    }

    /**
     * Generates a xml-file containing all member of {@code member}, which have
     * a iban and a bic, and prints the generated output into
     * {@code outputfile}. If {@code outputfile} already exists it will be
     * replaced. If it don´t it will be created.
     *
     * @param member The member to collect money via direct debit from.
     * @param contributions The mapping of membershipnumbers to contributions.
     * @param originator The originator of the direct debit.
     * @param sequenceType The sequence type of the direct debit.
     * @param outputfile The path to the file to print the xml to.
     * @param sepaWithBom Indicates whether to use UTF-8 with or without BOM.
     * @return A list containing member which are not included in the
     * outputfile. These are member which have no iban or no bic.
     */
    public static List<Member> createXMLFile(List<Member> member,
            Map<Integer, Double> contributions, Originator originator,
            SequenceType sequenceType, String outputfile, boolean sepaWithBom) {
        List<Member> invalidMember = filterValidMember(member);
        for (Member m : member) {
            if (!contributions.containsKey(m.getMembershipnumber())) {
                throw new IllegalArgumentException(
                        "No contribution specified at least for: " + m);
            }
        }
        IOStreamUtility.printContent(
                createXML(member, originator, contributions, sequenceType),
                outputfile, sepaWithBom);
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
            if (!hasValidIban(ah)) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has an invalid IBAN", m);
            }
            if (!ah.hasBic()) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has no BIC", m);
            }
            if (ah.getMandatSigned() == null) {
                valid = false;
                Logger.getLogger(SepaPain00800302XMLGenerator.class.getName())
                        .log(Level.WARNING, "{0} has a bad \"MandatErstellt\"",
                                m);
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
     * @param contributions The mapping of membershipnumbers to contributions.
     * @return The {@code String} representing the xml file content.
     */
    private static String createXML(List<Member> member, Originator originator,
            Map<Integer, Double> contributions, SequenceType sequenceType) {
        int numberOfTransactions = member.size();
        double controlSum = member.parallelStream()
                .mapToDouble(m -> contributions.get(m.getMembershipnumber()))
                .sum();
        //Eliminate double precision inaccuracy arose from IntStream
        controlSum = Math.rint(controlSum * 100) / 100;
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
                .append(SDF.format(new Date()))
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
                .append("       <SeqTp>")
                .append(sequenceType)
                .append("</SeqTp>\n")
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
                    .append(contributions.get(m.getMembershipnumber()))
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
