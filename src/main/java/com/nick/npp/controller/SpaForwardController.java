package com.nick.npp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {"/", "/dashboard", "/payid", "/send", "/payto", "/settlement", "/messages", "/about"})
    public String forward() {
        return "forward:/index.html";
    }
}
