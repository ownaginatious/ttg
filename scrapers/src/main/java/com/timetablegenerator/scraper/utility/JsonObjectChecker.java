package com.timetablegenerator.scraper.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashSet;
import java.util.Set;

public class JsonObjectChecker {

    private final JsonObject context;
    private final Set<String> keys = new HashSet<>();

    public JsonObjectChecker(JsonObject jo){

        this.context = jo;
        jo.entrySet().forEach(x -> keys.add(x.getKey()));
    }

    public JsonElement getJsonElement(String fieldName){

        JsonElement je = this.context.get(fieldName);

        if (je == null)
            throw new IllegalStateException("No such argument \"\" within: \"" + this.context + "\"");
        this.keys.remove(fieldName);

        return je;
    }

    public void checkCoverage(){
        if (!this.keys.isEmpty())
            throw new IllegalStateException("Unchecked object keys detected: " + this.keys);
    }
}
