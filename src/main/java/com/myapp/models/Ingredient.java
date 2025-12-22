package com.myapp.models;

/**
 * Represents an ingredient used in a recipe.
 */
public class Ingredient extends BaseModel {
    private String name;
    private double quantity;
    private String unit; // e.g., g, kg, ml, l, tsp, tbsp, cup, etc.
    private String notes; //  "chopped", "diced"

    // Constructors
    public Ingredient() {
    }

    public Ingredient(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (quantity > 0) {
            sb.append(quantity);
            if (unit != null && !unit.isEmpty()) {
                sb.append(" ").append(unit);
            }
            sb.append(" ");
        }
        sb.append(name);
        if (notes != null && !notes.isEmpty()) {
            sb.append(" (").append(notes).append(")");
        }
        return sb.toString();
    }
}
