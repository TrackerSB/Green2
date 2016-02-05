/**
 * For using Grün2 you need a database with at least the following tables and
 * attributes.
 * <ul>
 * <li>Mitglieder
 *     <ul>
 *     <li>Mitgliedsnummer (primary key)</li>
 *     <li>Titel</li>
 *     <li>Vorname</li>
 *     <li>Nachname</li>
 *     <li>istMaennlich (0=women, 1=men)</li>
 *     <li>Strasse</li>
 *     <li>Hausnummer</li>
 *     <li>PLZ</li>
 *     <li>Ort</li>
 *     <li>AusgetretenSeit (0000-00-00 if not resigned)</li>
 *     <li>IBAN</li>
 *     <li>BIC</li>
 *     <li>MandatErstellt (The date when the member signed the direct debit)</li>
 *     <li>KontoinhaberVorname (Only needed when Vorname is different from the prename of the account holder)</li>
 *     <li>KontoinhaberNachname (Only needed when Nachname is different from the lastname of the account holder)</li>
 *     <li>istBeitragsfrei (0=has to pay contribution, 1=has not to pay contribution)</li>
 *     </ul></li>
 * <li>Spitznamen
 *     <ul>
 *     <li>Name (The real name; primary key)</li>
 *     <li>Nichname (The associated name)</li>
 *     </ul></li>
 * </ul>
 */
package bayern.steinbrecher.gruen2;