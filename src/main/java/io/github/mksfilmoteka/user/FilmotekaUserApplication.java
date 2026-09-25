package io.github.mksfilmoteka.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FilmotekaUserApplication {

    static void main(String[] args) {
        SpringApplication.run(FilmotekaUserApplication.class, args);
    }

}
