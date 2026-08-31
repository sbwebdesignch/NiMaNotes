package com.nimanotes;

import com.nimanotes.util.NoteUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NoteUtilsTest {

    // ---- Diskrete Grenzwerte (Titel-Länge: gültiger Bereich 3..100) ----

    @Test
    void titleLengthLowerBoundary() {
        assertThat(NoteUtils.isTitleLengthValid("ab")).isFalse();  // n-1 (2)
        assertThat(NoteUtils.isTitleLengthValid("abc")).isTrue();  // n   (3)
        assertThat(NoteUtils.isTitleLengthValid("abcd")).isTrue(); // n+1 (4)
    }

    @Test
    void titleLengthUpperBoundary() {
        String len99 = "a".repeat(99);
        String len100 = "a".repeat(100);
        String len101 = "a".repeat(101);

        assertThat(NoteUtils.isTitleLengthValid(len99)).isTrue();   // n-1
        assertThat(NoteUtils.isTitleLengthValid(len100)).isTrue();  // n
        assertThat(NoteUtils.isTitleLengthValid(len101)).isFalse(); // n+1
    }

    @Test
    void titleLengthNullIsInvalid() {
        assertThat(NoteUtils.isTitleLengthValid(null)).isFalse();
    }

    // ---- Kontinuierliche Grenzwerte (Toleranzbereich ±0.001) ----

    @Test
    void computeRatioWithinTolerance() {
        double value = NoteUtils.computeRatio(22.0, 7.0);
        assertThat(value).isCloseTo(3.142857, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void computeRatioOutsideToleranceFailsAssertion() {
        double value = NoteUtils.computeRatio(1.0, 3.0); // 0.3333...
        // Negativtest: bewusst falscher Erwartungswert liegt klar ausserhalb der Toleranz
        assertThat(value).isNotCloseTo(0.5, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void computeRatioNegativeNumbers() {
        double value = NoteUtils.computeRatio(-10.0, 4.0);
        assertThat(value).isCloseTo(-2.5, org.assertj.core.data.Offset.offset(0.001));
    }

    // ---- Format & Muster (Regex) ----

    @Test
    void regexPatternValidAndInvalidEmail() {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        assertThat(NoteUtils.matchesPattern("test@example.com", regex)).isTrue();
        assertThat(NoteUtils.matchesPattern("not-an-email", regex)).isFalse();
        assertThat(NoteUtils.matchesPattern("missing-at-sign.com", regex)).isFalse();
        assertThat(NoteUtils.matchesPattern("", regex)).isFalse();
    }

    @Test
    void regexPatternSwissPostalCode() {
        String plzRegex = "^[1-9][0-9]{3}$";
        assertThat(NoteUtils.matchesPattern("4600", plzRegex)).isTrue();
        assertThat(NoteUtils.matchesPattern("0999", plzRegex)).isFalse(); // führende 0 unzulässig
        assertThat(NoteUtils.matchesPattern("46000", plzRegex)).isFalse(); // zu lang
    }

    @Test
    void matchesPatternNullInputIsFalse() {
        assertThat(NoteUtils.matchesPattern(null, "^[a-z]+$")).isFalse();
    }

    // ---- Fehlerbehandlung & Edge Cases ----

    @Test
    void divisionByZeroThrows() {
        assertThatThrownBy(() -> NoteUtils.computeRatio(1.0, 0.0)).isInstanceOf(IllegalArgumentException.class);
    }
}
