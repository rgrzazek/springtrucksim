package dev.truckcode.springtrucksim;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  @GetMapping("/")
  public String home(Model model) {
    model.addAttribute("message", "Welcome to the Spring Truck Sim!");
    return "home"; // Thymeleaf will look for home.html in src/main/resources/templates/
  }

  @GetMapping("/why")
  public String why(Model model) {
    return "commentary";
  }

  @GetMapping("/about_me")
  public String aboutMe(Model model) {
    return "about_me";
  }

  @GetMapping("/plans")
  public String plans(Model model) {
    return "plans";
  }

  @GetMapping("/sim")
  public String sim(Model model) {
    return "simulator";
  }

}
