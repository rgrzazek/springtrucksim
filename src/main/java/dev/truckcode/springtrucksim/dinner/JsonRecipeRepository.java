package dev.truckcode.springtrucksim.dinner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class JsonRecipeRepository implements RecipeRepository {

    private final List<Recipe> recipes;

    public JsonRecipeRepository(ObjectMapper objectMapper) throws IOException {
        var resource = new ClassPathResource("data/recipes.json");
        recipes = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }

    @Override
    public List<Recipe> findAll() {
        return Collections.unmodifiableList(recipes);
    }

    @Override
    public Optional<Recipe> findById(int id) {
        return recipes.stream().filter(r -> r.getId() == id).findFirst();
    }
}
