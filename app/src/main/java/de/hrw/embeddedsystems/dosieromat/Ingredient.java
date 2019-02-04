package de.hrw.embeddedsystems.dosieromat;

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
}
