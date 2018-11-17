package com.timetablegenerator;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Settings {

    private static final int DEFAULT_INDENT_SIZE = 4;

    private static String INDENT;

    static {
        setIndentSize(DEFAULT_INDENT_SIZE);
    }

    public static void setIndentSize(int tabSize){
        INDENT = IntStream.range(0, tabSize)
                .mapToObj(x -> " ")
                .collect(Collectors.joining(""));
    }

    public static String getIndent(){
        return INDENT;
    }
}
