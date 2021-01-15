package com.bb;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Random;

public class Sensor {
    private String name;
    private int factor;
    private String unit;
    private int lowerLimit;
    private int upperLimit;
    private float currentValue;

    @JsonCreator
    public Sensor(@JsonProperty("name") String name, @JsonProperty("factor") int factor,
                  @JsonProperty("unit") String unit, @JsonProperty("lowerLimit") int lowerLimit,
                  @JsonProperty("upperLimit") int upperLimit, @JsonProperty("currentValue") float currentValue) {
        this.name = name;
        this.factor = factor;
        this.unit = unit;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.currentValue = currentValue; // nema potreba ali eto da ne daje warning da nije iskoristen
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getFactor() {
        return factor;
    }
    public void setFactor(int factor) {
        this.factor = factor;
    }
    public String getUnit() {
        return unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public int getLowerLimit() {
        return lowerLimit;
    }
    public void setLowerLimit(int lowerLimit) {
        this.lowerLimit = lowerLimit;
    }
    public int getUpperLimit() {
        return upperLimit;
    }
    public void setUpperLimit(int upperLimit) {
        this.upperLimit = upperLimit;
    }
    public float getCurrentValue() {
        return this.currentValue;
    }
    public void setCurrentValue(float currentValue) {
        this.currentValue = currentValue;
    }

    private float generateRandomValue() {
        Random randomValue = new Random();
        // ovo je samo formula preko koje dobijem slucajnu vrijednost
        return (float)(randomValue.nextInt((upperLimit - lowerLimit) + 1) + lowerLimit) / factor;
    }

    // simulating reading from actual sensor device
    public void readCurrentValue() {
        float tmp = generateRandomValue();
        this.setCurrentValue(tmp);
    }

}
