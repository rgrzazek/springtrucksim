package dev.truckcode.springtrucksim.dinner;

import java.util.List;

public class Recipe {
    private int id;
    private String title;
    private List<String> ingredients;
    private String method;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public List<String> getIngredients() { return ingredients; }
    public void setIngredients(List<String> ingredients) { this.ingredients = ingredients; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}
