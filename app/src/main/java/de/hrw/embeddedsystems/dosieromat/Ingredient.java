package de.hrw.embeddedsystems.dosieromat;

import android.support.annotation.NonNull;

import java.util.Locale;

public class Ingredient {
    private String name;
    private int amount;
    private String unit;
    private boolean isAutoDispensed;

    public Ingredient(String name, int amount, String unit, boolean isAutoDispensed) {
        this.name = name;
        this.amount = amount;
        this.unit = unit;
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

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
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
        return String.format(Locale.GERMANY, "%1$d %2$s of %3$s. Dosieromat? %4$b", amount, unit, name, isAutoDispensed);
    }
}
