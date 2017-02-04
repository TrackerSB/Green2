/*
 * Copyright (c) 2017. Stefan Huber
 * This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Contains the hole application.
 * <p>
 * For using Green2 you need a MySQL database with at least the following tables
 * and attributes. (Green2 creates these tables for you if they not already
 * exist). This means at least for the first login you have to use an account
 * which is allowed to create tables. After the scheme is created you can use a
 * readonly account.
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
 * <li>AusgetretenSeit (0000-00-00 if not resigned)</li>
 * <li>IBAN</li>
 * <li>BIC</li>
 * <li>mandateErstellt (The date when the member signed the direct debit)</li>
 * <li>KontoinhaberVorname (Only needed when Vorname is different from the
 * first name of the account holder)</li>
 * <li>KontoinhaberNachname (Only needed when Nachname is different from the
 * last name of the account holder)</li>
 * <li>istBeitragsfrei (0=has to pay contribution, 1=has not to pay
 * contribution)</li>
 * <li>Beitrag (Optional. Used for setting individual contributions. If not
 * specified Green2 asks for a default contribution)</li>
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
