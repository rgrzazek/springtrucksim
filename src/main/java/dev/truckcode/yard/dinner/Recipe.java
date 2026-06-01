package dev.truckcode.yard.dinner;

import jakarta.persistence.*;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "recipe")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String slug;

    @Enumerated(EnumType.STRING)
    private RecipeSource source;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredient", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "position")
    private List<Ingredient> ingredients;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_method_step", joinColumns = @JoinColumn(name = "recipe_id"))
    @OrderColumn(name = "position")
    @Column(name = "step")
    private List<String> method;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
        this.slug = title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }

    public String getSlug() { return slug; }

    public List<Ingredient> getIngredients() { return ingredients; }
    public void setIngredients(List<Ingredient> ingredients) { this.ingredients = ingredients; }

    public String getIngredientNames() {
        return ingredients.stream().map(Ingredient::name).collect(Collectors.joining(","));
    }

    public List<String> getMethod() { return method; }
    public void setMethod(List<String> method) { this.method = method; }

    public RecipeSource getSource() { return source; }
    public void setSource(RecipeSource source) { this.source = source; }
}
