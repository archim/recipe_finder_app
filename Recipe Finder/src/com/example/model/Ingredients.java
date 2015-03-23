package com.example.model;

import java.util.Date;

/**
 * Archi M
 */
public class Ingredients implements Comparable<Ingredients> {

    private String item;
    private String unit;
    private String amount;
    private Date date;

    public Ingredients(String item, String unit, String amount) {
        this.item = item;
        this.unit = unit;
        this.amount = amount;
    }

    public Ingredients(String item, String unit, String amount, Date date) {
        this.item = item;
        this.unit = unit;
        this.amount = amount;
        this.date = date;
    }

    public String getItem() {
        return item;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setItem(String item) {
        this.item = item;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    @Override
    public int compareTo(Ingredients ingredients) {
        if (!this.item.equalsIgnoreCase(ingredients.item)) {
            return -1;
        } else if (!this.amount.equalsIgnoreCase(ingredients.amount)) {
            return -1;

        } else {
            //of course we can always put more logic around different units and compare
            //for example someone might put kilo of milk and in ingredients it's written mLs of milk
            //anyway ..skipping it for now and add it to TODO : pu more logic around units.
            final int unit1 = Integer.parseInt(this.unit);
            final int unit2 = Integer.parseInt(ingredients.unit);
            if (unit1 == unit2) {
                return 0;
            } else if (unit1 > unit2) {
                return 1;
            } else {
                return -1;
            }
        }

    }
}
