package com.nimanotes.integration;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integrationstests fuer den Schwerpunkt "Datenbankverbindung" (LB2).
 *
 * Es wird bewusst direkt ueber JDBC (statt ueber den Spring-Kontext) auf eine
 * H2-Datenbank im MariaDB-Kompatibilitaetsmodus zugegriffen, damit die
 * Verbindungslogik selbst - unabhaengig vom Connection-Pool von Spring Boot -
 * geprueft werden kann (T-001 bis T-005, siehe Testkonzept Kapitel 5.1).
 */
class DatabaseConnectionIntegrationTest {

    private String freshDbUrl() {
        // Jede Testmethode bekommt eine eigene, isolierte In-Memory-DB,
        // damit sich die Tests nicht gegenseitig beeinflussen.
        return "jdbc:h2:mem:conn_" + UUID.randomUUID() + ";MODE=MariaDB;DB_CLOSE_DELAY=-1";
    }

    // T-001: Erfolgreiche Verbindung zur Datenbank herstellen
    @Test
    void successfulConnectionToDatabaseIsEstablished() throws SQLException {
        String url = freshDbUrl();

        try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
            assertThat(connection).isNotNull();
            assertThat(connection.isValid(2)).isTrue();
            assertThat(connection.getMetaData().getURL()).contains("h2");
        }
    }

    // T-002: Verbindung mit falschen Zugangsdaten schlaegt kontrolliert fehl
    @Test
    void connectionWithWrongCredentialsIsRejected() throws SQLException {
        String url = freshDbUrl();

        // Erste Verbindung erstellt die DB und setzt ein Passwort fuer den User "sa".
        try (Connection setup = DriverManager.getConnection(url, "sa", "")) {
            try (Statement statement = setup.createStatement()) {
                statement.execute("ALTER USER sa SET PASSWORD 'korrektesPasswort'");
            }
        }

        // Verbindung mit falschem Passwort muss fehlschlagen.
        assertThatThrownBy(() -> DriverManager.getConnection(url, "sa", "falschesPasswort"))
                .isInstanceOf(SQLException.class);

        // Verbindung mit dem korrekten Passwort muss weiterhin funktionieren.
        try (Connection valid = DriverManager.getConnection(url, "sa", "korrektesPasswort")) {
            assertThat(valid.isValid(2)).isTrue();
        }
    }

    // T-003: Verbindung zu einer nicht erreichbaren Datenbank schlaegt kontrolliert fehl
    @Test
    void connectionToUnreachableDatabaseFails() {
        // Es wird ein TCP-Server auf einem Port angesprochen, auf dem kein H2-Server lauscht.
        // Das entspricht dem Szenario "DB ist nicht erreichbar" aus dem Testkonzept.
        String unreachableUrl = "jdbc:h2:tcp://localhost:1/mem:doesnotexist";

        assertThatThrownBy(() -> DriverManager.getConnection(unreachableUrl, "sa", ""))
                .isInstanceOf(SQLException.class);
    }

    // T-004: Nach dem Schliessen einer Verbindung kann eine neue, gueltige Verbindung
    // zur selben Datenbank aufgebaut werden und die Daten sind weiterhin vorhanden.
    @Test
    void reconnectAfterConnectionCloseKeepsDataAvailable() throws SQLException {
        String url = freshDbUrl();

        try (Connection first = DriverManager.getConnection(url, "sa", "")) {
            try (Statement statement = first.createStatement()) {
                statement.execute("CREATE TABLE ping(id INT PRIMARY KEY, status VARCHAR(50))");
                statement.execute("INSERT INTO ping VALUES (1, 'connected')");
            }
        }
        // erste Verbindung ist jetzt geschlossen (DB_CLOSE_DELAY=-1 haelt die DB dennoch am Leben)

        try (Connection second = DriverManager.getConnection(url, "sa", "")) {
            assertThat(second.isValid(2)).isTrue();
            try (Statement statement = second.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT status FROM ping WHERE id = 1")) {
                assertThat(resultSet.next()).isTrue();
                assertThat(resultSet.getString("status")).isEqualTo("connected");
            }
        }
    }

    // T-005: Mehrere aufeinanderfolgende Verbindungen sind stabil und unabhaengig gueltig
    @Test
    void multipleSequentialConnectionsAreStable() throws SQLException {
        String url = freshDbUrl();

        for (int i = 0; i < 5; i++) {
            try (Connection connection = DriverManager.getConnection(url, "sa", "")) {
                assertThat(connection.isValid(2)).isTrue();
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT 1")) {
                    assertThat(resultSet.next()).isTrue();
                    assertThat(resultSet.getInt(1)).isEqualTo(1);
                }
            }
        }
    }
}
