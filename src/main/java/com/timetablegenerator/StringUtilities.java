package com.timetablegenerator;

public class StringUtilities {

    public static String indent(int tabNum, String string){
        return indent(tabNum, 0, string);
    }

    public static String indent(int tabNum, int alignmentSpaces, String string){
        String indent = repeat(Settings.getIndent(), tabNum)
                + repeat(" ", alignmentSpaces);
        StringBuilder sb = new StringBuilder();
        for (String line : string.split("\n")){
            sb.append(indent).append(line).append('\n');
        }
        sb.setLength(sb.length() - 1); // Remove the last new line
        return sb.toString();
    }

    private static String repeat(String rep, int num){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++){
            sb.append(rep);
        }
        return sb.toString();
    }
}
