package dev.truckcode.springtrucksim.dinner;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RecipeService {

    private final RecipeRepository recipeRepository;

    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    public Recipe getTodaysRecipe() {
        List<Recipe> all = recipeRepository.findAll();
        int index = (int) (LocalDate.now().toEpochDay() % all.size());
        return all.get(index);
    }
}
