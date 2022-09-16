package kr.syeyoung.musicplayer.data;

import lombok.Data;
import org.bukkit.Location;

@Data
public class ClientVolumeSetting implements VolumeSettings {
    private double volume;
    private double minVolume;
    private Location location;
}
