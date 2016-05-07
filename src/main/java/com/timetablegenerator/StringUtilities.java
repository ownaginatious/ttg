package com.timetablegenerator;

public class StringUtilities {

    public static String indent(int tabNum, String string){
        StringBuilder sb = new StringBuilder();
        for (String line : string.split("\n")){
            sb.append(tabs(tabNum)).append(line).append('\n');
        }
        return sb.toString().trim();
    }

    private static String tabs(int num){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++){
            sb.append(Settings.getTab());
        }
        return sb.toString();
    }
}
