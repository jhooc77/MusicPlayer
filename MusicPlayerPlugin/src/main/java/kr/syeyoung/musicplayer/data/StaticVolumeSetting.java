package kr.syeyoung.musicplayer.data;

import lombok.AllArgsConstructor;
import lombok.Setter;
import org.bukkit.Location;

@AllArgsConstructor
public class StaticVolumeSetting implements VolumeSettings {
    @Setter
    private double volume = 1.0;

    public double getVolume() {
        return volume;
    }

    @Override
    public double getMinVolume() {
        return volume;
    }

    @Override
    public Location getLocation() {
        return new Location(null, 0, 0, 0);
    }
}
