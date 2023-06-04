package com.github.yeetmanlord.zetacorporations.util;

public class InputParser {

    public static double parseMoney(String input) {
        input = input.toLowerCase().replace(" ", "").replace(",", "").replace("$", "").replace("k", "000").replace("m", "000000").replace("b", "000000000");
        if (input.contains("e")) {
            String[] split = input.split("e");
            return Double.parseDouble(split[0]) * Math.pow(10, Double.parseDouble(split[1]));
        }
        return Double.parseDouble(input);
    }

    public static double parsePercent(String input) {
        input = input.toLowerCase().replace(" ", "").replace(",", "").replace("%", "");
        return Double.parseDouble(input) / 100;
    }

    public static long parseLong(String input) {
        input = input.toLowerCase().replace(" ", "").replace(",", "").replace("k", "000").replace("m", "000000").replace("b", "000000000");
        if (input.contains("e")) {
            String[] split = input.split("e");
            return (long) (Double.parseDouble(split[0]) * Math.pow(10, Double.parseDouble(split[1])));
        }
        return Long.parseLong(input);
    }
}
