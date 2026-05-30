package dev.truckcode.springtrucksim.dinner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashSet;
import java.util.List;

@Controller
public class DinnerController {

    private static final Logger log = LoggerFactory.getLogger(DinnerController.class);
    private final RecipeService recipeService;
    private final GeminiService geminiService;

    public DinnerController(RecipeService recipeService, GeminiService geminiService) {
        this.recipeService = recipeService;
        this.geminiService = geminiService;
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
        model.addAttribute("ingredientNames", recipeService.getAllIngredientNames());
        return "dinner";
    }

    @PostMapping("/dinner/ai-recipe")
    public String aiRecipe(@RequestParam List<String> ingredients, Model model) {
        var approved = new HashSet<>(recipeService.getAllIngredientNames());
        var safe = ingredients.stream().filter(approved::contains).toList();
        if (safe.isEmpty()) {
            return "redirect:/dinner";
        }
        try {
            model.addAttribute("recipe", geminiService.generateRecipe(safe));
            return "recipe";
        } catch (Exception e) {
            log.error("Gemini recipe generation failed", e);
            return "redirect:/dinner";
        }
    }
}
