package com.mk.jwtauth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.sql.SQLException;

@SpringBootApplication
public class JwtauthApplication {

	public static void main(String[] args) throws SQLException {

		SpringApplication.run(JwtauthApplication.class, args);
//		jdbc_viwa.main();
	}

}
