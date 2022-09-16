package kr.syeyoung.musicplayer.data;

import org.bukkit.Location;

public interface VolumeSettings {
    double getVolume();

    double getMinVolume();

    Location getLocation();
}
