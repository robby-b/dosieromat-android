package de.hrw.embeddedsystems.dosieromat;

import android.support.annotation.NonNull;

import java.util.Locale;

/*
Klasse die eine einzelne Zutat in einem Rezept dastellt

 */
public class Ingredient {
    private String name; // Name der Zutat
    private int amount; // Menge der Zutat in Gramm
    private boolean isAutoDispensed; // Ob die Zutat von einem Dosieromaten automatisch dosiert werden kann oder manuell hinzugegeben werden muss

    public Ingredient(String name, int amount, String unit, boolean isAutoDispensed) {
        this.name = name;
        this.amount = amount;
        this.isAutoDispensed = isAutoDispensed;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public boolean isAutoDispensed() {
        return isAutoDispensed;
    }

    public void setAutoDispensed(boolean autoDispensed) {
        isAutoDispensed = autoDispensed;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.GERMANY, "%1$d grams of %3$s. Dosieromat? %4$b", amount, name, isAutoDispensed);
    }
}
