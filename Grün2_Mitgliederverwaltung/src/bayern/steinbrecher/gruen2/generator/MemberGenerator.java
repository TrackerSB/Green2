package bayern.steinbrecher.gruen2.generator;

import bayern.steinbrecher.gruen2.member.AccountHolder;
import bayern.steinbrecher.gruen2.member.Address;
import bayern.steinbrecher.gruen2.member.Member;
import bayern.steinbrecher.gruen2.member.Person;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Converts String results of database into member.
 *
 * @author Stefan Huber
 */
public class MemberGenerator {

    /**
     * Prohibit construction.
     */
    private MemberGenerator() {
        throw new UnsupportedOperationException(
                "Construction of an object is not allowed.");
    }

    /**
     * Generates a list of member out of {@code queryResult}.
     *
     * @param queryResult The table that hold the member informations. First
     * dimension has to be row; second column. Each row is treated as one
     * member.
     * @return The resulting list of member.
     */
    public static List<Member> generateMemberList(
            List<List<String>> queryResult) {
        List<String> labels = queryResult.get(0).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
        List<Member> memberList = new ArrayList<>(queryResult.size());

        int prenameIndex = labels.indexOf("vorname");
        int lastnameIndex = labels.indexOf("nachname");
        int titleIndex = labels.indexOf("titel");
        int birthdayIndex = labels.indexOf("geburtstag");
        int isMaleIndex = labels.indexOf("istmaennlich");
        int ibanIndex = labels.indexOf("iban");
        int bicIndex = labels.indexOf("bic");
        int mandatCreatedIndex = labels.indexOf("mandaterstellt");
        int streetIndex = labels.indexOf("strasse");
        int housenumberIndex = labels.indexOf("hausnummer");
        int postcodeIndex = labels.indexOf("plz");
        int placeIndex = labels.indexOf("ort");
        int isActiveIndex = labels.indexOf("istaktiv");
        int isContributionfreeIndex = labels.indexOf("istbeitragsfrei");
        int membershipnumberIndex = labels.indexOf("mitgliedsnummer");
        int accountholderPrenameIndex = labels.indexOf("kontoinhabervorname");
        int accountholderLastnameIndex = labels.indexOf("kontoinhabernachname");

        queryResult.parallelStream().skip(1).forEach(row -> {
            //Read attributes
            LocalDate birthday = null;
            LocalDate mandatsigned = null;
            try {
                birthday = LocalDate.parse(row.get(birthdayIndex));
                mandatsigned = LocalDate.parse(row.get(mandatCreatedIndex));
            } catch (DateTimeParseException ex) {
                Logger.getLogger(MemberGenerator.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            boolean isMale = row.get(isMaleIndex).equalsIgnoreCase("1");
            boolean isActive = row.get(isActiveIndex).equalsIgnoreCase("1");
            boolean isContributionfree
                    = row.get(isContributionfreeIndex).equalsIgnoreCase("1");
            int membershipnumber = 0;
            try {
                membershipnumber
                        = Integer.parseInt(row.get(membershipnumberIndex));
            } catch (NumberFormatException ex) {
                Logger.getLogger(MemberGenerator.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            String accountholderPrename = row.get(accountholderPrenameIndex);
            if (accountholderPrename.isEmpty()) {
                accountholderPrename = row.get(prenameIndex);
            }
            String accountholderLastname = row.get(accountholderLastnameIndex);
            if (accountholderLastname.isEmpty()) {
                accountholderLastname = row.get(lastnameIndex);
            }

            //Connect attributes
            Person p = new Person(row.get(prenameIndex), row.get(lastnameIndex),
                    row.get(titleIndex), birthday, isMale);
            //FIXME MandatChanged has not to be always false
            AccountHolder ah = new AccountHolder(row.get(ibanIndex),
                    row.get(bicIndex), mandatsigned, false,
                    accountholderPrename, accountholderLastname,
                    row.get(titleIndex), birthday, isMale);
            Address ad = new Address(row.get(streetIndex),
                    row.get(housenumberIndex), row.get(postcodeIndex),
                    row.get(placeIndex));
            Member m = new Member(
                    membershipnumber, p, ad, ah, isActive, isContributionfree);

            synchronized (memberList) {
                memberList.add(m);
            }
        });

        return memberList;
    }
}
