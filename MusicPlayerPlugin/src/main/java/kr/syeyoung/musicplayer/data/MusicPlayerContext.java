package kr.syeyoung.musicplayer.data;

import kr.syeyoung.musicplayer.Musicplayer;
import kr.syeyoung.musicplayer.SinewaveRegistry;
import kr.syeyoung.musicplayer.sound.SoundController;
import lombok.Data;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.quifft.output.FFTFrame;
import org.quifft.output.FrequencyBin;

import java.io.File;
import java.util.UUID;

@Data
public class MusicPlayerContext implements Runnable {
    private UUID musicId;
    private Player player;
    private File file;
    private VolumeSettings volumeSettings;
    private FFTProvider provider;
    private SoundController controller;
    private int range = -1;
    private boolean debug = false;
    private SinewaveRegistry sinewaveRegistry;
    private boolean doStopSound;

    @Override
    public void run() { // Probably shouldn't play song here but idk
        player.sendMessage("Playing... "+file.getAbsolutePath());
        double minF = 0.5;
        double maxF = 2;
        final long startTime = System.currentTimeMillis();
        final double value = Musicplayer.getPlugin(Musicplayer.class).getConfig().getDouble("filter-percent", 0.05);
        while(true) {
            if (!provider.prepareFrame()) {
                break;
            }

            double volume = volumeSettings.getVolume();
            Location loc = volumeSettings.getLocation();
            loc.setWorld(player.getWorld());
            if (volumeSettings.getVolume() - Math.sqrt(loc.distanceSquared(player.getLocation())) / 16 < volumeSettings.getMinVolume()) {
                volume = volumeSettings.getMinVolume();
                loc = player.getLocation();
            }

            FFTFrame frame = provider.getFrame();
            if (debug) player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(frame.frameStartMs+"/"+frame.frameEndMs));

            int cnt = 0;
            FrequencyBin lastBin = frame.bins[0];
            boolean increasing = false;
//            for (FrequencyBin bin : frame.bins) {
//                if (bin.amplitude < lastBin.amplitude) {
//                    if (lastBin.amplitude > 0.05 && increasing) {
//                        String s = SinewaveRegistry.getBestSound(lastBin.frequency);
//                        double freq = SinewaveRegistry.getFrequency(s);
//                        player.playSound(loc, s, SoundCategory.BLOCKS, (float) (volume * lastBin.amplitude), (float) (lastBin.frequency / freq));
//                        cnt++;
//                    }
//                    increasing = false;
//                } else {
//                    increasing = true;
//                }
//                lastBin = bin;
//            }
            if (doStopSound) {
                for (String sound : sinewaveRegistry.SOUND_FREQUENCY.keySet()) {
                    player.stopSound(sound);
                }
            }
            for (FrequencyBin bin : frame.bins) {
                if (bin.amplitude > value) {
                    String s = sinewaveRegistry.getBestSound(bin.frequency);
                    double freq = sinewaveRegistry.getFrequency(s);
                    double fl = (bin.frequency / freq);
                    if (range != -1) {
                        controller.writeSoundNearby(s, (float) (volume * bin.amplitude), (float) fl, range);
                    } else {
                        controller.writeSound(s, (float) (volume * bin.amplitude), (float) fl);
                    }
                    cnt++;
                    if (fl < minF) {
                        minF = fl;
                    } else if (fl > maxF) {
                        maxF = fl;
                    }
                }
            }
            controller.flushSound();
            long sleepTime = (long) (startTime + frame.frameEndMs - System.currentTimeMillis());

            if (debug) player.sendTitle("count:" + cnt, "min:" + String.format("%.3f", minF) + " max:" + String.format("%.3f", maxF) + " time:" + sleepTime, 0, 40, 10);

            try {
                Thread.sleep(sleepTime < 0 ? 0 : sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        player.sendMessage("SONG FINISH: "+file.getAbsolutePath());
    }
}
