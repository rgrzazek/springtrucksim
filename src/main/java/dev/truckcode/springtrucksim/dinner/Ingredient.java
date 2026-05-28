package dev.truckcode.springtrucksim.dinner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public record Ingredient(
        String name,
        String quantity,
        String display,
        String type
) {
    @JsonCreator
    public Ingredient(
            @JsonProperty("name") String name,
            @JsonProperty("quantity") String quantity,
            @JsonProperty("display") String display,
            @JsonProperty("type") String type
    ) {
        this.name = name;
        this.quantity = quantity;
        this.display = display;
        this.type = type;
    }
}
