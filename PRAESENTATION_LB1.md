# LB1 Praesentations-Leitfaden

Diese Datei erklaert **jede Testklasse**, **was sie beweist**, **welche Zeile im
Programmcode dafuer zustaendig ist** und **wie du live einen Fehler einbauen
kannst**, damit der Test rot wird. Damit kannst du der Lehrperson zeigen, dass
die Tests echte Fehler erkennen (Kriterium B: "Identifikation neuer Fehler").

Am Ende steht eine Checkliste, was du vor der Vorfuehrung noch erledigen musst.

> Alle "Fehler live einbauen"-Szenarien in dieser Datei wurden vorher tatsaechlich
> mit `mvn test` durchgespielt (Fehler einbauen -> Test rot -> zurueck -> Test
> gruen). Das ist keine Vermutung, sondern verifiziert.

---

## Uebersicht: Welche Datei deckt welches Kriterium ab

| Kriterium (Bewertungsraster) | Testklasse | Getestete Unit |
|---|---|---|
| Test-Kategorie 1: Diskrete Grenzwerte | `NoteUtilsTest` (Zeile 11-34) | `NoteUtils.isTitleLengthValid()` |
| Test-Kategorie 2: Kontinuierliche Grenzwerte | `NoteUtilsTest` (Zeile 36-62) | `NoteUtils.computeRatio()` |
| Test-Kategorie 3: Format & Muster (Regex) | `NoteUtilsTest` (Zeile 64-86) | `NoteUtils.matchesPattern()` |
| Test-Kategorie 4: Kombinatorische Logik | `NoteServiceTest` | `NoteService.canCreateNote()` |
| Test 2: Freie Wahl (Faktor 3) | `SessionCleanupServiceTest` | `SessionCleanupService.purgeExpiredSessions()` |
| Test 3a/3b: TDD | `PasswordPolicyTest` + `TDD_Design_PasswordPolicy.md` | `PasswordPolicy` |
| Zusatz (kein eigenes Kriterium) | `LoginSessionServiceTest`, `NoteRepositoryTest` | Hilfstests |

---

## 1. `NoteUtilsTest.java` — 3 der 4 Pflichtkategorien

Getestete Klasse: `src/main/java/com/nimanotes/util/NoteUtils.java`

### 1a. Diskrete Grenzwerte (Zeile 11-34)

Testet `NoteUtils.isTitleLengthValid(title)` — gueltiger Bereich ist Laenge
3 bis 100 Zeichen (Quelle: `NoteUtils.java` Zeile 10-15).

- `titleLengthLowerBoundary()` (Zeile 13-18): prueft die untere Grenze exakt
  bei n-1 (2 Zeichen, muss `false` sein), n (3 Zeichen, `true`) und n+1
  (4 Zeichen, `true`).
- `titleLengthUpperBoundary()` (Zeile 20-29): dasselbe fuer die obere Grenze
  bei 99/100/101 Zeichen.
- `titleLengthNullIsInvalid()` (Zeile 31-33): Negativtest fuer `null`.

**Fehler live einbauen:** In `NoteUtils.java` Zeile 14 `len <= 100` zu
`len <= 99` aendern. Dann schlaegt `titleLengthUpperBoundary()` fehl, weil
ein 100-Zeichen-Titel neu als ungueltig gilt, obwohl der Test `isTrue()`
erwartet.

### 1b. Kontinuierliche Grenzwerte (Zeile 36-62)

Testet `NoteUtils.computeRatio(a, b)` mit Toleranzbereich ±0.001
(Quelle: `NoteUtils.java` Zeile 17-21).

- `computeRatioWithinTolerance()` (Zeile 38-42): 22/7 ≈ 3.142857, Positivtest.
- `computeRatioOutsideToleranceFailsAssertion()` (Zeile 44-50): 1/3 = 0.333...
  darf klar **nicht** nahe bei 0.5 liegen — Negativtest mit `isNotCloseTo`.
- `computeRatioNegativeNumbers()` (Zeile 52-56): -10/4 = -2.5, negative Werte.
- `computeRatioDivisionByZeroThrows()` (Zeile 58-62): Division durch 0 muss
  eine `IllegalArgumentException` werfen.

**Fehler live einbauen:** In `NoteUtils.java` Zeile 20 `return a / b;` zu
`return a / b + 1;` aendern. `computeRatioWithinTolerance()` und
`computeRatioNegativeNumbers()` schlagen sofort fehl, weil die Toleranz von
0.001 weit ueberschritten wird.

### 1c. Format & Muster / Regex (Zeile 64-86)

Testet `NoteUtils.matchesPattern(input, regex)`
(Quelle: `NoteUtils.java` Zeile 23-27).

- `regexPatternValidAndInvalidEmail()` (Zeile 66-73): gueltige E-Mail,
  ungueltige Strings ohne "@", leerer String.
- `regexPatternSwissPostalCode()` (Zeile 75-81): Schweizer PLZ-Regex,
  inkl. Negativtest fuer fuehrende Null und zu lange Eingabe.
- `matchesPatternNullInputIsFalse()` (Zeile 83-85): `null`-Eingabe.

**Fehler live einbauen:** In `NoteUtils.java` Zeile 24-25 den Null-Check
entfernen (`if (input == null) return false;` loeschen). Dann wirft
`matchesPatternNullInputIsFalse()` eine `NullPointerException` statt eines
sauberen `false` — der Test schlaegt mit einem Error fehl statt einer
einfachen Assertion, was gut zeigt, dass der Test auch Exceptions abfaengt.

---

## 2. `NoteServiceTest.java` — Kombinatorische Logik (4. Pflichtkategorie)

Getestete Klasse: `src/main/java/com/nimanotes/service/NoteService.java`,
Methode `canCreateNote(user, currentNotes, isPremium)` (Zeile 13-18).

Die Methode hat 3 verschachtelte Bedingungen — die Tests decken alle
Kombinationen der Entscheidungstabelle ab:

| Test (Zeile) | user | notes | premium | Erwartung |
|---|---|---|---|---|
| `nullUserCannotCreate` (46-51) | `null` | egal | egal | `false` |
| `nullNotesListIsAllowed` (40-44) | vorhanden | `null` | `false` | `true` |
| `nonPremiumUnderLimitCanCreate` (18-24) | vorhanden | 2 Notizen | `false` | `true` |
| `nonPremiumAtLimitCannotCreate` (26-31) | vorhanden | 5 Notizen | `false` | `false` |
| `premiumIgnoresLimit` (33-37) | vorhanden | 5 Notizen | `true` | `true` |

**Fehler live einbauen:** In `NoteService.java` Zeile 17
`currentNotes.size() < DEFAULT_LIMIT` zu `currentNotes.size() <= DEFAULT_LIMIT`
aendern (Off-by-one-Fehler). `nonPremiumAtLimitCannotCreate()` schlaegt fehl,
weil bei genau 5 Notizen faelschlicherweise wieder `true` zurueckkommt.

---

## 3. `SessionCleanupServiceTest.java` — Freie Wahl, Faktor 3

Getestete Klasse: `src/main/java/com/nimanotes/service/SessionCleanupService.java`,
Methode `purgeExpiredSessions(clock, ttl)` (Zeile 24-41).

Kombiniert zwei fortgeschrittene Techniken (das begruendet Faktor 3 statt nur 1):

1. **Time-Freezing**: `Clock.fixed(...)` (Zeile 39) statt echter Systemzeit —
   damit ist die "Ablaufzeit" fuer den Test exakt reproduzierbar.
2. **Erweitertes Mocking**: `Mockito.@Mock` fuer das Repository, `InOrder`
   fuer die Aufrufreihenfolge (Zeile 57-59), `doThrow` fuer eine simulierte
   DB-Exception (Zeile 82).

Testfaelle:
- `deletesOnlyExpiredSessionsOldestFirst()` (Zeile 42-60): 2 abgelaufene
  Sessions (unterschiedlich alt) und 1 gueltige. Prueft, dass nur die
  abgelaufenen geloescht werden **und** in der richtigen Reihenfolge
  (aeltere zuerst, weil `purgeExpiredSessions` vorher sortiert,
  `SessionCleanupService.java` Zeile 26).
- `noSessionsAreDeletedWhenAllWithinTtl()` (Zeile 62-73): Nullfall, nichts
  wird geloescht.
- `continuesCleanupWhenDeletionOfOneSessionFails()` (Zeile 75-89): Simuliert
  eine DB-Exception beim Loeschen einer Session — die Bereinigung der
  uebrigen Sessions muss trotzdem weiterlaufen (`try/catch` in
  `SessionCleanupService.java` Zeile 31-37).

**Wo laeuft das wirklich in der App?** `LoginView.java` legt bei jedem Login
einen `LoginSession`-Datensatz an (siehe unten). `SessionCleanupScheduler.java`
ruft `purgeExpiredSessions()` automatisch alle 15 Minuten auf
(`@Scheduled`). Das ist wichtig zu erwaehnen, falls die Lehrperson fragt,
wo diese Funktion im Produkt tatsaechlich verwendet wird — es ist kein
reiner Test-Selbstzweck.

**Fehler live einbauen:** In `SessionCleanupService.java` Zeile 26
`sessions.sort(...)` entfernen (Sortierung weglassen). Der Test
`deletesOnlyExpiredSessionsOldestFirst()` schlaegt fehl, weil die
`InOrder`-Pruefung eine andere Loeschreihenfolge feststellt (das Mock in
Zeile 49 liefert die beiden abgelaufenen Sessions absichtlich nicht in
chronologischer Reihenfolge zurueck — nur wenn die Methode selbst sortiert,
kommt die erwartete Reihenfolge heraus). Ich habe das vor dem Schreiben
dieser Anleitung mit `mvn test` verifiziert: mit Sortierung gruen, ohne
Sortierung faellt genau dieser Test rot.

Alternativ: Zeile 34 `catch (RuntimeException e)` zu
`catch (IllegalStateException e)` aendern. Dann fliegt die simulierte
`RuntimeException` aus `continuesCleanupWhenDeletionOfOneSessionFails()`
ungefangen durch die Methode, und der Test schlaegt mit einem Error fehl
statt eine saubere Assertion zu pruefen.

---

## 4. `PasswordPolicyTest.java` — TDD (Auftrag 3)

Ablauf, den du der Lehrperson erzaehlen kannst:

1. **Design zuerst** (`TDD_Design_PasswordPolicy.md`): Schnittstelle und
   Regeln wurden festgelegt, **bevor** `PasswordPolicy.java` existierte.
2. **Tests zuerst** (`PasswordPolicyTest.java`): Wurden gegen die
   noch nicht existierende Klasse geschrieben.
3. **Dann Implementierung** (`PasswordPolicy.java`), mit einem bewusst
   eingebauten Fehler.
4. **Fehler dokumentiert**: `M450_LB1_Unittest_Fehler.xlsx`, Status "Offen".

Getestete Klasse: `src/main/java/com/nimanotes/util/PasswordPolicy.java`.

| Test (Zeile) | Regel | Datei-Zeile in `PasswordPolicy.java` |
|---|---|---|
| `nullOrBlankPasswordIsInvalid` (16-20) | null/leer ungueltig | 16-19 |
| `minLengthBoundary` (22-27) | Grenzwert 7/8/9 Zeichen | 20-22 |
| `missingUppercaseIsInvalid` (29-33) | Grossbuchstabe fehlt | 23-25 |
| `missingLowercaseIsInvalid` (35-39) | Kleinbuchstabe fehlt | 26-28 |
| `missingDigitIsInvalid` (41-45) | Ziffer fehlt | 29-31 |
| `missingSpecialCharIsInvalid` (47-51) | Sonderzeichen fehlt | 32-34 |
| `allRulesSatisfiedIsValid` (53-57) | Happy Path | — |
| `whitespaceIsNotAcceptedAsSpecialChar` (59-66) | **bewusster Fehler**, `@Disabled` | 54-59 |

**Der bewusst eingebaute Fehler** (fuer die Live-Demo besonders wichtig):

`PasswordPolicy.java` Zeile 59:
```java
return password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
```
Das Leerzeichen `' '` ist weder Buchstabe noch Ziffer, wird also
faelschlicherweise als "Sonderzeichen" akzeptiert. Ein Passwort wie
`"Abc12345 "` (Leerzeichen statt `!`) gilt dadurch als gueltig, obwohl es
das eigentlich nicht sollte.

**So zeigst du den Fehler live:**
1. Oeffne `PasswordPolicyTest.java` Zeile 59-61.
2. Entferne die Zeile `@Disabled("FEHLER-001 ...")` (Zeile 60-61) — die
   `@Test`-Annotation (Zeile 59) bleibt stehen.
3. Fuehre die Tests aus (`mvn test` oder Rechtsklick > Run). Der Test
   `whitespaceIsNotAcceptedAsSpecialChar` schlaegt jetzt fehl, weil
   `PasswordPolicy.isValid("Abc12345 ")` `true` statt `false` liefert.
4. Das ist **exakt der Fehler**, der in `M450_LB1_Unittest_Fehler.xlsx`
   dokumentiert ist. Status bleibt "Offen" — laut Aufgabenstellung muss er
   nicht behoben werden.
5. Danach `@Disabled(...)` wieder einfuegen, damit die Gesamt-Suite wieder
   komplett gruen ist.

**Wo wird `PasswordPolicy` echt verwendet?** `RegisterView.java` Zeile 54-58
ruft `PasswordPolicy.getViolations(password)` auf und zeigt die Verletzungen
dem Nutzer an, bevor ein Konto angelegt wird — die TDD-Unit ist also aktiv
im Produkt eingebunden, nicht nur Testmaterial.

---

## 5. Zusatztests (kein eigenes Bewertungs-Kriterium, aber gute Praxis)

- `LoginSessionServiceTest.java`: einfacher Time-Freezing-Test fuer
  `LoginSessionService.isExpired()` — die Basis-Unit, auf der
  `SessionCleanupService` aufbaut.
- `NoteRepositoryTest.java`: `@DataJpaTest`-Integrationstest gegen eine
  echte H2-Datenbank (speichern/laden von `User` und `Note`). Zeigt, dass
  ihr auch mit einer echten Persistenzschicht arbeitet, zaehlt aber laut
  Aufgabenstellung nicht als eigene "Test-Kategorie".

Falls die Lehrperson danach fragt: Diese zwei Klassen sind bewusst **nicht**
Teil der 4 Pflichtkategorien oder von Test 2 — reine Ergaenzung.

---

## Automatisierungsgrad (3 Punkte fuer "nach push")

`.github/workflows/tests.yml` fuehrt bei jedem `git push` automatisch
`mvn test` aus (GitHub Actions) und laedt die Testberichte als Artefakt hoch.
**Wichtig:** Dafuer muss der aktuelle Stand tatsaechlich auf GitHub liegen —
sonst kannst du der Lehrperson keinen erfolgreichen Workflow-Lauf zeigen.
Siehe Checkliste unten.

---

## Checkliste vor der Praesentation

1. [ ] **Push zum richtigen Repo mit dem richtigen Account.** Aktuell schlaegt
       `git push origin main` mit "permission denied" fehl, weil das
       gespeicherte GitHub-Login (`nico584`) offenbar keine Schreibrechte auf
       `sbwebdesignch/NiMaNotes` hat. Klaert das im Team: Wer ist Owner/Admin
       des Repos, wer muss als Collaborator eingeladen werden, oder ist das
       richtige Ziel-Repo vielleicht `upstream`
       (`https://github.com/NitRam222/NiMaNotes.git`)? Ohne das laeuft die
       CI-Pipeline nie und ihr bekommt die 3 Automatisierungs-Punkte nicht.
2. [ ] Nach erfolgreichem Push: auf GitHub unter "Actions" pruefen, dass der
       Workflow "Unit Tests" gruen durchlaeuft.
3. [ ] `mvn test` lokal einmal frisch laufen lassen und die Konsolen-Ausgabe
       (28 grüne + 1 disabled) griffbereit haben.
4. [ ] `M450_LB1_Unittest_Fehler.xlsx` offen haben, um sie bei Bedarf direkt
       zu zeigen.
5. [ ] Diese Datei (`PRAESENTATION_LB1.md`) als Spickzettel nutzen, um schnell
       zur richtigen Zeile zu springen, wenn die Lehrperson "zeig mir, dass
       der Test einen Fehler findet" sagt.
6. [ ] E-Mail an die Lehrperson (Team, Programm, Technologien, mind. 3
       Entitaeten) nochmals gegenchecken — das ist Teil der Bewertung,
       aber nicht im Code, das kann ich nicht fuer dich pruefen.
