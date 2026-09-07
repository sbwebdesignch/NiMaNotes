package com.nimanotes.ui;

import com.nimanotes.model.User;
import com.nimanotes.repository.UserRepository;
import com.nimanotes.util.PasswordPolicy;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Route("register")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterView(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

        setSizeFull();
        addClassName("auth-view");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("Konto erstellen");
        TextField usernameField = new TextField("Benutzername");
        PasswordField passwordField = new PasswordField("Passwort");
        Button registerButton = new Button("Konto anlegen");
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button backButton = new Button("Zum Login");
        backButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        registerButton.addClickListener(event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            if (username == null || username.isBlank() || password == null || password.isBlank()) {
                Notification.show("Bitte alle Felder füllen");
                return;
            }

            List<String> passwordViolations = PasswordPolicy.getViolations(password);
            if (!passwordViolations.isEmpty()) {
                Notification.show("Passwort zu schwach: " + String.join("; ", passwordViolations));
                return;
            }

            if (userRepository.findByUsername(username).isPresent()) {
                Notification.show("Benutzername ist schon vorhanden");
                return;
            }

            User user = new User(username, passwordEncoder.encode(password));
            userRepository.save(user);
            Notification.show("Konto erstellt");
            UI.getCurrent().navigate("login");
        });

        backButton.addClickListener(event -> UI.getCurrent().navigate("login"));

        VerticalLayout card = new VerticalLayout(title, usernameField, passwordField, registerButton, backButton);
        card.addClassName("auth-card");
        card.setPadding(false);
        card.setSpacing(true);

        add(card);
    }
}
