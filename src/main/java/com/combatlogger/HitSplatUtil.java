package com.combatlogger;

import net.runelite.api.HitsplatID;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class HitSplatUtil
{
    private static final Map<Integer, String> hitsplatNames = initializeHitsplatNames();

    private static Map<Integer, String> initializeHitsplatNames()
    {
        Map<Integer, String> names = new HashMap<>();
        Class<?> hitsplatIdClass = HitsplatID.class;

        for (Field field : hitsplatIdClass.getDeclaredFields())
        {
            try
            {
                int value = field.getInt(null);
                String name = field.getName();
                names.put(value, name);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        return names;
    }

    public static String getHitsplatName(int hitsplatType)
    {
        return hitsplatNames.getOrDefault(hitsplatType, "Unknown");
    }
}
