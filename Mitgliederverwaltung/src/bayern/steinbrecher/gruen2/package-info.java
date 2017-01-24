/*
 * Copyright (c) 2017. Stefan Huber
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * Contains the hole application.
 *
 * For using Grün2 you need a MySQL database with at least the following tables
 * and attributes. (Grün2 creates these tables for you if they not already
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
 * <li>MandatErstellt (The date when the member signed the direct debit)</li>
 * <li>KontoinhaberVorname (Only needed when Vorname is different from the
 * prename of the account holder)</li>
 * <li>KontoinhaberNachname (Only needed when Nachname is different from the
 * lastname of the account holder)</li>
 * <li>istBeitragsfrei (0=has to pay contribution, 1=has not to pay
 * contribution)</li>
 * <li>Beitrag (Optional. Used for setting individual contributions. If not
 * specified Grün2 asks for a default contribution)</li>
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
package bayern.steinbrecher.gruen2;
