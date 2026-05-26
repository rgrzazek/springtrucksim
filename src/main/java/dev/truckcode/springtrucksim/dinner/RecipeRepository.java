package dev.truckcode.springtrucksim.dinner;

import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    List<Recipe> findAll();
    Optional<Recipe> findById(int id);
}
