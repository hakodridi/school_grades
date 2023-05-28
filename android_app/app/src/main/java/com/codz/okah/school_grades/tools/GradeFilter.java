package com.codz.okah.school_grades.tools;

import android.text.InputFilter;
import android.text.Spanned;

public class GradeFilter  implements InputFilter {

    private double minValue;
    private double maxValue;

    public GradeFilter() {
        this.minValue = 0;
        this.maxValue = 20;
    }



    private boolean isInRange(double value) {
        return value >= minValue && value <= maxValue;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        try {
            String newVal = dest.subSequence(0, dstart) + source.toString() + dest.subSequence(dend, dest.length());

            double input = Double.parseDouble(newVal);
            if (isInRange(input))
                return null;
        } catch (NumberFormatException ignored) {}

        return "";
    }
}


