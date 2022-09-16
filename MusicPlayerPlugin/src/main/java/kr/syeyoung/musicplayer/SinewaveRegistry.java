package kr.syeyoung.musicplayer;

import org.bukkit.Sound;

import java.util.*;

public class SinewaveRegistry {
    public TreeMap<Double, String> FREQUENCY_SOUND = new TreeMap<>();
    public Map<String, Double> SOUND_FREQUENCY = new HashMap<>();
    public SinewaveRegistry(String resourceName) {
        register(0.0, "minecraft:" + resourceName + ".0");
        register(10.0, "minecraft:" + resourceName + ".10");
        register(20.0, "minecraft:" + resourceName + ".20");
        register(40.0, "minecraft:" + resourceName + ".40");
        register(80.0, "minecraft:" + resourceName + ".80");
        register(160.0, "minecraft:" + resourceName + ".160");
        register(320.0, "minecraft:" + resourceName + ".320");
        register(640.0, "minecraft:" + resourceName + ".640");
        register(1280.0, "minecraft:" + resourceName + ".1280");
        register(2560.0, "minecraft:" + resourceName + ".2560");
        register(5120.0, "minecraft:" + resourceName + ".5120");
        register(10240.0, "minecraft:" + resourceName + ".10240");
    }

    public SinewaveRegistry(String resourceName, List<Integer> waveList) {
        for (int integer : waveList) {
            if (integer % 10 == 0) {
                int t = integer/10;
                if ((t & (t-1)) == 0) {
                    register((double) integer, "minecraft:" + resourceName + "." + integer);
                }
            }
        }
    }

    private void register(Double freq, String sound) {
        FREQUENCY_SOUND.put(freq, sound);
        SOUND_FREQUENCY.put(sound, freq);
    }

    public double getFrequency(String s) {
        return SOUND_FREQUENCY.get(s);
    }

    public String getBestSound(double freq) {
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
