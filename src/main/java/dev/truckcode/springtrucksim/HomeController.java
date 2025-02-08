package dev.truckcode.springtrucksim;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("message", "Welcome to the Spring Truck Sim!");
    return "home"; // Thymeleaf will look for home.html in src/main/resources/templates/
  }
}
