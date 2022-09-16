package kr.syeyoung.musicplayer;

import org.bukkit.Sound;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class SinewaveRegistry {
    public static TreeMap<Double, String> FREQUENCY_SOUND = new TreeMap<>();
    public static Map<String, Double> SOUND_FREQUENCY = new HashMap<>();
    static {
        register(0.0, "minecraft:sinewaves.0");
        register(10.0, "minecraft:sinewaves.10");
        register(20.0, "minecraft:sinewaves.20");
        register(40.0, "minecraft:sinewaves.40");
        register(80.0, "minecraft:sinewaves.80");
        register(160.0, "minecraft:sinewaves.160");
        register(320.0, "minecraft:sinewaves.320");
        register(640.0, "minecraft:sinewaves.640");
        register(1280.0, "minecraft:sinewaves.1280");
        register(2560.0, "minecraft:sinewaves.2560");
        register(5120.0, "minecraft:sinewaves.5120");
        register(10240.0, "minecraft:sinewaves.10240");
    }

    private static void register(Double freq, String sound) {
        FREQUENCY_SOUND.put(freq, sound);
        SOUND_FREQUENCY.put(sound, freq);
    }

    public static double getFrequency(String s) {
        return SOUND_FREQUENCY.get(s);
    }

    public static String getBestSound(double freq) {
        Double key2 = FREQUENCY_SOUND.floorKey(freq);
        Double key1 = FREQUENCY_SOUND.ceilingKey(freq);
        if (key2 == null) return FREQUENCY_SOUND.get(key1);
        if (0.5 <Math.abs(freq / key2) && Math.abs(freq / key2) < 2.0) {
            return FREQUENCY_SOUND.get(key2);
        } else {
            return FREQUENCY_SOUND.get(key2);
        }
    }
}
