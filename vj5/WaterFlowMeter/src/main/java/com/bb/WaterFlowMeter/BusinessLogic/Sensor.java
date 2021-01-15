package com.bb.WaterFlowMeter.BusinessLogic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Random;

//@JsonIgnoreProperties(value={"map", "getMetoda"}) // ne vrijedi za metode,
// a metode koje Jackson gleda imaji prefiks get i set
public class Sensor {
    private String name; // should be unique
    private int factor;
    private String unit;
    private int lowerLimit;
    private int upperLimit;
    private float currentValue;
    @JsonIgnore
    private HashMap<String, Float> map = new HashMap<>();

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
    public HashMap<String, Float> getMap() {
        return map;
    }
    public void setMap(HashMap<String, Float> map) {
        this.map = map;
    }

    private float generateRandomValue() {
        Random randomValue = new Random();
        return (float)(randomValue.nextInt((upperLimit - lowerLimit) + 1) + lowerLimit) / factor;
    }

    public float readCurrentValue() {
        // simulating reading from actual sensor device
        this.currentValue = generateRandomValue();
        return this.currentValue;
    }

    public void addToNotPublished(float value) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        this.map.put(dtf.format(now), value);
    }

    public String jsonNormalized() {
        // returns json that is safe to nest inside another json
        // replaces " with \"
        try {
            return new ObjectMapper().writeValueAsString(this).replace("\"", "\\\"");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error serializing sensor to json string in getJsonAsString()");
            return null;
        }
    }
}
