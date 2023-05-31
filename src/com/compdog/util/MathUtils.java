package com.compdog.util;

public class MathUtils {
    public static final float PI = (float)Math.PI;

    public static float Lerp(float a, float b, float t)
    {
        return a*(1.0f-t)+b*t;
    }

    public static float Sin(float x)
    {
        return (float)Math.sin(x);
    }

    public static float NormSin(float x)
    {
        return (Sin(x) + 1.0f) / 2.0f;
    }
}
