package fsa.training.travelee.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {
    @GetMapping("/page/home")
    public String showPageHome() {
        return "page/home";
    }

    @GetMapping("/oauth2/authorization/google")
    public String showGoogleLogin() {
        return "oauth2/authorization/google";
    }
}
