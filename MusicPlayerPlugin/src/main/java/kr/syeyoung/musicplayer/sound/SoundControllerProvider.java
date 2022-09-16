package kr.syeyoung.musicplayer.sound;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class SoundControllerProvider {

    private static final String version = Bukkit.getServer().getClass().getPackage().getName().substring(Bukkit.getServer().getClass().getPackage().getName().lastIndexOf('.') + 2);

    public static SoundController bukkitController(Player player) {
        return new SoundController(player) {
            @Override
            public void writeSound(String sound, float volume, float pitch) {
                player.playSound(player.getLocation(), sound, volume, pitch);
            }

            @Override
            public void writeSoundNearby(String sound, float volume, float pitch, int range) {
                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                    if (onlinePlayer.getWorld().equals(player.getWorld())) {
                        if (onlinePlayer.getLocation().distanceSquared(player.getLocation()) < range*range) {
                            onlinePlayer.playSound(player.getLocation(), sound, volume, pitch);
                        }
                    }
                }
            }

            @Override
            public void flushSound() {

            }
        };
    }

    public static SoundController nettyController(Player player) {
        switch(version) {
            case "1_17_R1": return new SoundController1_17_R1(player);
            case "1_18_R2": return new SoundController1_18_R2(player);
            case "1_12_R1": return new SoundController1_12_R2(player);
            default: return bukkitController(player);
        }
    }


}
