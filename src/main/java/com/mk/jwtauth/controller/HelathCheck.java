package com.mk.jwtauth.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelathCheck {
    @GetMapping("/healthCheck")
    public String helathCheck() {
        return "200 OK";
    }
}
