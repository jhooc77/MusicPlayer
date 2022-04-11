package kr.syeyoung.musicplayer.data;

import kr.syeyoung.musicplayer.SinewaveRegistry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
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

    @Override
    public void run() { // Probably shouldn't play song here but idk
        player.sendMessage("Playing... "+file.getAbsolutePath());
        double minF = 0.5;
        double maxF = 2;
        final long startTime = System.currentTimeMillis();
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
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(frame.frameStartMs+"/"+frame.frameEndMs));

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
            for (FrequencyBin bin : frame.bins) {
                if (bin.amplitude > 0.05) {
                    String s = SinewaveRegistry.getBestSound(bin.frequency);
                    double freq = SinewaveRegistry.getFrequency(s);
                    double fl = (bin.frequency / freq);
                    player.playSound(loc, s, SoundCategory.BLOCKS, (float) (volume * bin.amplitude), (float) fl);
                    cnt++;
                    if (fl < minF) {
                        minF = fl;
                    } else if (fl > maxF) {
                        maxF = fl;
                    }
                }
            }
            long sleepTime = (long) (startTime + frame.frameEndMs - System.currentTimeMillis());

            player.sendTitle("count:" + cnt, "min:" + String.format("%.3f", minF) + " max:" + String.format("%.3f", maxF) + " time:" + sleepTime, 0, 40, 10);

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        player.sendMessage("SONG FINISH: "+file.getAbsolutePath());
    }
}
