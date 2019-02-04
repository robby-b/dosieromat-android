package de.hrw.embeddedsystems.dosieromat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Recipe {
    private String name;
    private List<Ingredient> ingredients;
    private int amount;
    private String unit;

    private Recipe(String name, int amount, String unit, List<Ingredient> ingredients) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.ingredients = ingredients;
    }

    public static Recipe loadFromFile() {
        List<Ingredient> ingredients = new ArrayList<>();


        return new Recipe("Kaffee", 1, "Kanne", new ArrayList<Ingredient>());
    }




}
