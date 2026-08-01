package com.mk.jwtauth;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class JwtauthApplication {

	public static void main(String[] args) throws SQLException {
		loadDotenv();
		SpringApplication.run(JwtauthApplication.class, args);
	}

	public static void loadDotenv() {
		try {
			Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
			dotenv.entries().forEach(entry -> {
				if (System.getProperty(entry.getKey()) == null) {
					System.setProperty(entry.getKey(), entry.getValue());
				}
			});
		} catch (Exception e) {
			// Ignores if .env file is missing
		}
	}
}
