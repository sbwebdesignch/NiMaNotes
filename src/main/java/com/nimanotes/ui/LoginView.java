package com.nimanotes.ui;

import com.nimanotes.model.LoginSession;
import com.nimanotes.repository.LoginSessionRepository;
import com.nimanotes.repository.UserRepository;
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
import com.vaadin.flow.server.VaadinServletRequest;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.util.UUID;

@Route("login")
@AnonymousAllowed
public class LoginView extends VerticalLayout {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final LoginSessionRepository loginSessionRepository;

    public LoginView(AuthenticationManager authenticationManager, UserRepository userRepository,
            LoginSessionRepository loginSessionRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.loginSessionRepository = loginSessionRepository;

        setSizeFull();
        addClassName("auth-view");
        setAlignItems(FlexComponent.Alignment.CENTER);
        setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);

        H1 title = new H1("NiMaNotes Login");
        TextField usernameField = new TextField("Benutzername");
        PasswordField passwordField = new PasswordField("Passwort");
        Button loginButton = new Button("Einloggen");
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        Button registerButton = new Button("Konto erstellen");
        registerButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        loginButton.addClickListener(event -> {
            String username = usernameField.getValue();
            String password = passwordField.getValue();

            try {
                Authentication authenticated = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(username, password));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authenticated);
                SecurityContextHolder.setContext(context);

                VaadinServletRequest.getCurrent().getHttpServletRequest()
                    .getSession(true)
                    .setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

                userRepository.findByUsername(username).ifPresent(user -> {
                    LoginSession session = new LoginSession(UUID.randomUUID().toString(), user);
                    loginSessionRepository.save(session);
                });

                UI.getCurrent().navigate("");
            } catch (AuthenticationException e) {
                Notification.show("Login fehlgeschlagen");
            }
        });

        registerButton.addClickListener(event -> UI.getCurrent().navigate("register"));

        VerticalLayout card = new VerticalLayout(title, usernameField, passwordField, loginButton, registerButton);
        card.addClassName("auth-card");
        card.setPadding(false);
        card.setSpacing(true);

        add(card);
    }
}
