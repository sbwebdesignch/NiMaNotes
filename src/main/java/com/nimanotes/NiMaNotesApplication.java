package com.nimanotes;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Theme("nimanotes")
public class NiMaNotesApplication implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(NiMaNotesApplication.class, args);
    }
}
