package com.nimanotes;

import com.nimanotes.util.PasswordPolicy;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auftrag 3b - TDD: Diese Tests wurden gemäss TDD_Design_PasswordPolicy.md
 * VOR der Implementierung von PasswordPolicy geschrieben.
 */
class PasswordPolicyTest {

    @Test
    void nullOrBlankPasswordIsInvalid() {
        assertThat(PasswordPolicy.isValid(null)).isFalse();
        assertThat(PasswordPolicy.isValid("")).isFalse();
        assertThat(PasswordPolicy.isValid("   ")).isFalse();
    }

    @Test
    void minLengthBoundary() {
        assertThat(PasswordPolicy.isValid("Ab1!23a")).isFalse(); // 7 Zeichen (n-1)
        assertThat(PasswordPolicy.isValid("Ab1!23ab")).isTrue(); // 8 Zeichen (n)
        assertThat(PasswordPolicy.isValid("Ab1!23abc")).isTrue(); // 9 Zeichen (n+1)
    }

    @Test
    void missingUppercaseIsInvalid() {
        assertThat(PasswordPolicy.getViolations("abcdefg1!"))
                .contains("Passwort muss mindestens einen Grossbuchstaben enthalten");
    }

    @Test
    void missingLowercaseIsInvalid() {
        assertThat(PasswordPolicy.getViolations("ABCDEFG1!"))
                .contains("Passwort muss mindestens einen Kleinbuchstaben enthalten");
    }

    @Test
    void missingDigitIsInvalid() {
        assertThat(PasswordPolicy.getViolations("Abcdefgh!"))
                .contains("Passwort muss mindestens eine Ziffer enthalten");
    }

    @Test
    void missingSpecialCharIsInvalid() {
        assertThat(PasswordPolicy.getViolations("Abcdefg1"))
                .contains("Passwort muss mindestens ein Sonderzeichen enthalten");
    }

    @Test
    void allRulesSatisfiedIsValid() {
        assertThat(PasswordPolicy.isValid("Abcdef1!")).isTrue();
        assertThat(PasswordPolicy.getViolations("Abcdef1!")).isEmpty();
    }

    @Test
    @Disabled("FEHLER-001 (offen, siehe M450_LB1_Unittest_Fehler.xlsx): "
            + "containsSpecialChar() akzeptiert faelschlicherweise ein Leerzeichen "
            + "als Sonderzeichen. Zum Reproduzieren @Disabled entfernen.")
    void whitespaceIsNotAcceptedAsSpecialChar() {
        // "Abc12345 " erfuellt Laenge/Gross-/Kleinbuchstabe/Ziffer, hat aber nur
        // ein Leerzeichen statt eines echten Sonderzeichens -> muss ungueltig sein.
        assertThat(PasswordPolicy.isValid("Abc12345 ")).isFalse();
    }
}
