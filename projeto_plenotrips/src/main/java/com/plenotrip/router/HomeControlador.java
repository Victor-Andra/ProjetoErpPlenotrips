package com.plenotrip.router;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.plenotrip.nucleo.util.JwtUtil;

@Controller
public class HomeControlador {

    @GetMapping("/")
    public String home() {
        return "index"; // redireciona para templates/index.html
    }
}