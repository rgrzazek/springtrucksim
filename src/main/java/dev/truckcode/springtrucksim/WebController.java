package dev.truckcode.springtrucksim;

import dev.truckcode.springtrucksim.dinner.RecipeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

  private final RecipeService recipeService;

  public WebController(RecipeService recipeService) {
    this.recipeService = recipeService;
  }

  @GetMapping("/")
  public String home() {
    return "home";
  }

  @GetMapping("/about_me")
  public String aboutMe() {
    return "about_me";
  }

  @GetMapping("/sim")
  public String sim() {
    return "simulator";
  }

  @GetMapping("/dinner")
  public String dinner(Model model) {
    model.addAttribute("recipe", recipeService.getTodaysRecipe());
    return "dinner";
  }

}
