package com.cpiss.genunie;

import android.util.Log;

public class L {
    public static void l(String value) {
        Log.d("NVakti", "" + value);
    }

    public static void l(int value) {
        l(">>" + value);
    }

    public static void l(float value) {
        l(">>" + value);
    }

    public static void l(double value) {
        l(">>" + value);
    }

}
