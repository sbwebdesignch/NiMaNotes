package com.nimanotes.util;

import java.util.ArrayList;
import java.util.List;

public final class PasswordPolicy {

    private static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static List<String> getViolations(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null || password.isBlank()) {
            violations.add("Passwort darf nicht leer sein");
            return violations;
        }
        if (password.length() < MIN_LENGTH) {
            violations.add("Passwort muss mindestens " + MIN_LENGTH + " Zeichen lang sein");
        }
        if (!containsUppercase(password)) {
            violations.add("Passwort muss mindestens einen Grossbuchstaben enthalten");
        }
        if (!containsLowercase(password)) {
            violations.add("Passwort muss mindestens einen Kleinbuchstaben enthalten");
        }
        if (!containsDigit(password)) {
            violations.add("Passwort muss mindestens eine Ziffer enthalten");
        }
        if (!containsSpecialChar(password)) {
            violations.add("Passwort muss mindestens ein Sonderzeichen enthalten");
        }
        return violations;
    }

    public static boolean isValid(String password) {
        return getViolations(password).isEmpty();
    }

    private static boolean containsUppercase(String password) {
        return password.chars().anyMatch(Character::isUpperCase);
    }

    private static boolean containsLowercase(String password) {
        return password.chars().anyMatch(Character::isLowerCase);
    }

    private static boolean containsDigit(String password) {
        return password.chars().anyMatch(Character::isDigit);
    }

    private static boolean containsSpecialChar(String password) {
        // Bekannter Fehler (siehe M450_LB1_Unittest_Fehler.xlsx, Status: Offen):
        // isLetterOrDigit(c) == false trifft auch auf Leerzeichen zu, wodurch ein
        // Passwort mit Leerzeichen statt einem echten Sonderzeichen fälschlicherweise
        // als gültig durchgeht.
        return password.chars().anyMatch(c -> !Character.isLetterOrDigit(c));
    }
}
