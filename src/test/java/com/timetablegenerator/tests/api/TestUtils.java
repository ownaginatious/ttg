package com.timetablegenerator.tests.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestUtils {

    private static Random random = new Random();

    public static String getRandomString(int maxLength){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < maxLength; i++) {
            sb.append((char) random.nextInt((122 - 32) + 1));
        }
        return sb.toString();
    }

    public static List<String> getRandomStrings(int number, int maxLength) {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < number; i++){
            strings.add(getRandomString(maxLength));
        }
        return strings;
    }
}
