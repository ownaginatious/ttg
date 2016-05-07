package com.timetablegenerator;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Settings {

    private static final int DEFAULT_TAB_SIZE = 4;

    private static String TAB;

    static {
        setTabSize(DEFAULT_TAB_SIZE);
    }

    public static void setTabSize(int tabSize){
        TAB = IntStream.range(0, tabSize)
                .mapToObj(x -> " ")
                .collect(Collectors.joining(""));
    }

    public static String getTab(){
        return TAB;
    }
}
