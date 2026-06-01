package dev.truckcode.yard.dinner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RecipeSeeder implements CommandLineRunner {

    private final RecipeRepository recipeRepository;
    private final ObjectMapper objectMapper;

    public RecipeSeeder(RecipeRepository recipeRepository, ObjectMapper objectMapper) {
        this.recipeRepository = recipeRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void run(String... args) throws Exception {
        if (recipeRepository.count() > 0) return;

        var json = new JsonRecipeRepository(objectMapper);
        var recipes = json.findAll();
        recipes.forEach(r -> {
            r.setId(null);
            r.setSource(RecipeSource.HOUSE);
        });
        recipeRepository.saveAll(recipes);
    }
}
