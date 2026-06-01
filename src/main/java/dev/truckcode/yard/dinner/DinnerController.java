package dev.truckcode.yard.dinner;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class DinnerController {

    private static final Logger log = LoggerFactory.getLogger(DinnerController.class);
    private final RecipeService recipeService;
    private final GeminiService geminiService;
    private final ObjectMapper objectMapper;

    @Value("classpath:data/ingredients.json")
    private Resource ingredientsResource;

    private List<String> ingredientOptions;

    public DinnerController(RecipeService recipeService, GeminiService geminiService, ObjectMapper objectMapper) {
        this.recipeService = recipeService;
        this.geminiService = geminiService;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadIngredients() throws Exception {
        var type = objectMapper.getTypeFactory().constructCollectionType(List.class, String.class);
        ingredientOptions = objectMapper.readValue(ingredientsResource.getInputStream(), type);
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
        model.addAttribute("tonightsRecipe", recipeService.getTodaysRecipe());
        model.addAttribute("ingredientOptions", ingredientOptions);
        return "dinner";
    }

    @PostMapping("/dinner/ai-recipe")
    @ResponseBody
    public ResponseEntity<Map<String, String>> aiRecipe(@RequestParam List<String> ingredients, HttpSession session) {
        var allowed = new HashSet<>(ingredientOptions);
        var safe = ingredients.stream().filter(allowed::contains).toList();
        if (safe.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No valid ingredients selected"));
        }
        return switch (geminiService.generateRecipe(safe)) {
            case GeminiResult.Success s -> {
                session.setAttribute("pendingRecipe", s.recipe());
                session.setAttribute("pendingToken", UUID.randomUUID().toString());
                yield ResponseEntity.ok(Map.of("redirect", "/dinner/ai-preview"));
            }
            case GeminiResult.OverCapacity ignored ->
                ResponseEntity.status(503).body(Map.of("error",
                        "Gemini's out of free thoughts for now because someone skimped on the API budget. Try again tomorrow."));
            case GeminiResult.Failure f -> {
                log.error("Gemini recipe generation failed", f.cause());
                yield ResponseEntity.status(500).body(Map.of("error", "Gemini couldn't generate a recipe. Try again."));
            }
        };
    }

    @GetMapping("/dinner/ai-preview")
    public String aiPreview(HttpSession session, Model model) {
        var recipe = session.getAttribute("pendingRecipe");
        var token = session.getAttribute("pendingToken");
        if (recipe == null || token == null)
            return "redirect:/dinner";
        model.addAttribute("recipe", recipe);
        model.addAttribute("isPreview", true);
        model.addAttribute("token", token);
        return "recipe";
    }

    @PostMapping("/dinner/submit")
    @ResponseBody
    public ResponseEntity<Map<String, String>> submitRecipe(@RequestParam String token, HttpSession session) {
        var recipe = (Recipe) session.getAttribute("pendingRecipe");
        var expected = (String) session.getAttribute("pendingToken");
        if (recipe == null || expected == null || !expected.equals(token)) {
            return ResponseEntity.status(410).body(Map.of("error", "This recipe has expired. Generate a new one."));
        }
        if (recipeService.getRecipeBySlug(recipe.getSlug()).isPresent()) {
            return ResponseEntity.status(409).body(Map.of("error", "You already have a recipe with that name."));
        }
        recipe.setSource(RecipeSource.SUBMITTED);
        recipeService.save(recipe);
        session.removeAttribute("pendingRecipe");
        session.removeAttribute("pendingToken");
        return ResponseEntity.ok(Map.of("redirect", "/dinner/" + recipe.getSlug()));
    }
}
