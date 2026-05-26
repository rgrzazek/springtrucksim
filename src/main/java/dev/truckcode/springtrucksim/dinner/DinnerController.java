package dev.truckcode.springtrucksim.dinner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DinnerController {

    private final RecipeService recipeService;

    public DinnerController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/dinner")
    public String dinner(Model model) {
        model.addAttribute("recipe", recipeService.getTodaysRecipe());
        return "dinner";
    }
}
