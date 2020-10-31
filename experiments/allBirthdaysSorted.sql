SELECT m.Mitgliedsnummer, m.Vorname, m.Nachname, m.Titel, m.Geburtstag, m.IstAktiv, m.IstEhrenmitglied, m.MitgliedSeit, m.MitwirkendSeit, m.IstMaennlich, m.Strasse, m.Hausnummer, m.PLZ, m.Ort, m.`MitwirkendSeit`, m.`25MitgliedGeehrt`, m.`40MitgliedGeehrt`, CONCAT_WS(' ', IF(m.IstMaennlich, 'Lieber', 'Liebe'), IFNULL(s.Spitzname, m.Vorname)) AS Anrede
FROM `Mitglieder` m LEFT OUTER JOIN `Spitznamen` s ON m.`Vorname` = s.`Name`
WHERE `AusgetretenSeit` IS NULL
    AND (
        2021 - YEAR(m.Geburtstag) = 50
            OR 2021 - YEAR(m.Geburtstag) = 60
            OR 2021 - YEAR(m.Geburtstag) = 70
            OR 2021 - YEAR(m.Geburtstag) = 75
            OR 2021 - YEAR(m.Geburtstag) >= 80
    )
ORDER BY YEAR(m.Geburtstag) DESC, MONTH(m.Geburtstag) ASC, DAYOFMONTH(m.Geburtstag) ASC;

