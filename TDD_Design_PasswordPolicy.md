# Auftrag 3a - TDD Design (ohne Code): PasswordPolicy

## Ausgangslage / Lücke

Die Registrierung (`RegisterView`) prüft aktuell nur, ob Benutzername und
Passwort nicht leer sind. Es gibt keine Prüfung der Passwort-Sicherheit.
Das ist eine fehlende, aber sicherheitsrelevante Funktion.

## Ziel-Unit

Neue Klasse `com.nimanotes.util.PasswordPolicy` (zustandslose Utility-Klasse,
analog zu `NoteUtils`).

## Öffentliche Schnittstelle (Vertrag, vor der Implementierung festgelegt)

- `List<String> getViolations(String password)`
  Gibt eine Liste aller verletzten Regeln zurück (leer = Passwort gültig).
- `boolean isValid(String password)`
  Kurzform: `true`, wenn `getViolations(...)` leer ist.

## Regeln (mehrere Pfade)

1. Passwort darf nicht `null` oder leer/blank sein.
2. Mindestlänge 8 Zeichen.
3. Mindestens ein Grossbuchstabe.
4. Mindestens ein Kleinbuchstabe.
5. Mindestens eine Ziffer.
6. Mindestens ein echtes Sonderzeichen (Satz-/Symbolzeichen) - **Leerzeichen
   zählt explizit NICHT als Sonderzeichen**, da es sonst ein Passwort wie
   `"Abc12345 "` fälschlicherweise akzeptieren würde.

## Geplante Testfälle (vor der Implementierung, Positiv- und Negativtests)

- `null` / leerer String -> ungültig
- Länge 7 vs. 8 vs. 9 (Grenzwertanalyse)
- Kein Grossbuchstabe -> ungültig
- Kein Kleinbuchstabe -> ungültig
- Keine Ziffer -> ungültig
- Kein Sonderzeichen -> ungültig
- Alle Regeln erfüllt (z. B. `"Abcdef1!"`) -> gültig (Happy Path)
- **Risikofall**: Passwort mit Leerzeichen statt echtem Sonderzeichen
  (z. B. `"Abc12345 "`) -> muss ungültig bleiben

Dieser letzte Testfall wurde bewusst _vor_ der Implementierung entworfen,
weil er ein realistisches Risiko für einen Implementierungsfehler markiert
(Verwechslung von "kein Buchstabe/keine Ziffer" mit "ist ein Sonderzeichen").
Siehe `PasswordPolicyTest.java` und `M450_LB1_Unittest_Fehler.xlsx` für das
Ergebnis dieses Tests nach der Implementierung.
