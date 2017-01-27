package com.litbikes.util;

import java.awt.Color;
import java.util.Random;

public class ColourUtil {
    private static final float MIN_BRIGHTNESS = 0.8f;
    public static Color getBrightColor() {
        Random random = new Random();
        float h = random.nextFloat();
        float s = random.nextFloat();
        float b = MIN_BRIGHTNESS + ((1f - MIN_BRIGHTNESS) * random.nextFloat());
        Color c = Color.getHSBColor(h, s, b);
        return c;
    }
    
}
