package dev.truckcode.springtrucksim.dinner;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DinnerController {

    private final RecipeService recipeService;

    public DinnerController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }

    @GetMapping("/tonight")
    public String tonight() {
        return "redirect:/dinner/" + recipeService.getTodaysRecipe().getSlug();
    }

    @GetMapping("/dinner/{slug}")
    public String recipe(@PathVariable String slug, Model model) {
        var recipe = recipeService.getRecipeBySlug(slug);
        if (recipe.isEmpty()) {
            return "redirect:/dinner";
        }
        model.addAttribute("recipe", recipe.get());
        return "recipe";
    }

    @GetMapping("/dinner")
    public String dinnerList(Model model) {
        model.addAttribute("recipes", recipeService.getAllRecipes());
        return "dinner";
    }
}
