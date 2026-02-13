package com.example.neuro_gamesense1.coreService;

public class ThermalPredictor {
    public static double score(double[] input) {
        return 1.3812837170698415 + input[0] * 1.0145935142263485 + input[1] * 0.0008692291931943483 + input[2] * -0.000004593707546492751 + input[3] * -0.0018137718807037127;
    }
}
