package dev.truckcode.yard.dinner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class JsonRecipeRepository {

    private final List<Recipe> recipes;

    public JsonRecipeRepository(ObjectMapper objectMapper) throws IOException {
        var resource = new ClassPathResource("data/recipes.json");
        recipes = objectMapper.readValue(resource.getInputStream(), new TypeReference<>() {});
    }


    public List<Recipe> findAll() {
        return Collections.unmodifiableList(recipes);
    }


    public Optional<Recipe> findById(int id) {
        return recipes.stream().filter(r -> r.getId() == id).findFirst();
    }


    public Optional<Recipe> findBySlug(String slug) {
        return recipes.stream().filter(r -> r.getSlug().equals(slug)).findFirst();
    }
}
