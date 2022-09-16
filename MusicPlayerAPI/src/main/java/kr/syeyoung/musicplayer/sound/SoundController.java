package kr.syeyoung.musicplayer.sound;

import org.bukkit.entity.Player;

public abstract class SoundController {
    Player player;

    public SoundController(Player player) {
        this.player = player;
    }

    public abstract void writeSound(String sound, float volume, float pitch);

    public abstract void writeSoundNearby(String sound, float volume, float pitch, int range);

    public abstract void flushSound();

}
