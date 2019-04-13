package de.hrw.embeddedsystems.dosieromat;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
Klasse für ein Rezept
Wird aus der XML-Datei gelesen
 */
public class Recipe {
    private String name;
    private List<Ingredient> ingredients;
    private int amount;
    private String unit;

    public Recipe(String name, int amount, String unit, List<Ingredient> ingredients) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
        this.ingredients = ingredients;
    }

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format(Locale.GERMANY, "Rezept: %1$d %2$s %3$s:", amount, unit, name));
        sb.append('\n');

        for (Ingredient i : ingredients) {
            sb.append(i.toString());
            sb.append('\n');
        }

        return sb.toString();
    }

    /*
    Diese Methode wandelt ein Rezept in eine Liste von Anweisungen für den Mikrocontroller um
     */
    public List<String> toCommandList(int numOfPortions) {
        List<String> commands = new ArrayList<>();

        for (Ingredient i : ingredients) {
            if(i.isAutoDispensed()) {
                StringBuilder sb = new StringBuilder();
                sb.append(i.getName());
                sb.append(';');
                sb.append(i.getAmount() * numOfPortions);
                commands.add(sb.toString());
            }
        }

        return commands;
    }
}
