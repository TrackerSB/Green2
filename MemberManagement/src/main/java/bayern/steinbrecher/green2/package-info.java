/**
 * Contains the hole application.
 * <p>
 * For using Green2 you need a MySQL database with at least the following tables and attributes. (Green2 creates these
 * tables for you if they not already exist). This means at least for the first login you have to use an account which
 * is allowed to create tables. After the scheme is created you can use a readonly account.
 * <ol>
 * <li>Mitglieder
 * <ul>
 * <li>Mitgliedsnummer (primary key)</li>
 * <li>Titel</li>
 * <li>Vorname</li>
 * <li>Nachname</li>
 * <li>istAktiv (0=passive, 1=aktive)</li>
 * <li>istMaennlich (0=women, 1=men)</li>
 * <li>Geburtstag</li>
 * <li>Strasse</li>
 * <li>Hausnummer</li>
 * <li>PLZ</li>
 * <li>Ort</li>
 * <li>AusgetretenSeit (null if not resigned)</li>
 * <li>IBAN</li>
 * <li>BIC</li>
 * <li>mandateErstellt (The date when the member signed the direct debit)</li>
 * <li>KontoinhaberVorname (Only needed when Vorname is different from the first name of the account holder)</li>
 * <li>KontoinhaberNachname (Only needed when Nachname is different from the last name of the account holder)</li>
 * <li>istBeitragsfrei (0=has to pay contribution, 1=has not to pay contribution)</li>
 * <li>Beitrag (Optional. Used for setting individual contributions. If not specified Green2 asks for a default
 * contribution)</li>
 * </ul>
 * </li>
 * <li>Spitznamen
 * <ul>
 * <li>Name (The real name; primary key)</li>
 * <li>Spitzname (The associated name)</li>
 * </ul>
 * </li>
 * </ol>
 *
 * @version 2u12
 */
package bayern.steinbrecher.green2;
