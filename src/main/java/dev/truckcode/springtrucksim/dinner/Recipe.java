package dev.truckcode.springtrucksim.dinner;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private List<String> ingredients;
    private List<String> method;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public List<String> getMethod() { return method; }
    public void setMethod(List<String> method) { this.method = method; }

    public String getSlug() {
        return title.toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
    }
}
