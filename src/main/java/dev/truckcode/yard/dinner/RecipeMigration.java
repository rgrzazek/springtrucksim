package dev.truckcode.yard.dinner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RecipeMigration implements CommandLineRunner {

    private final RecipeRepository recipeRepository;

    public RecipeMigration(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @Override
    public void run(String... args) {
        recipeRepository.findAll().stream()
                .filter(r -> r.getSource() == null)
                .forEach(r -> {
                    r.setSource(RecipeSource.HOUSE);
                    recipeRepository.save(r);
                });
    }
}
