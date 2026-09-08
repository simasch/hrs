package ch.martinelli.edu.hrs;

import org.springframework.boot.SpringApplication;

public class TestHrsApplication {

    public static void main(String[] args) {
        SpringApplication.from(HrsApplication::main).run(args);
    }

}
