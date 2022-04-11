package kr.syeyoung.musicplayer;

import kr.syeyoung.musicplayer.data.MusicPlayerContext;
import kr.syeyoung.musicplayer.data.StaticVolumeSetting;
import kr.syeyoung.musicplayer.data.WavFileFFTFrameProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Plugin(name="MusicPlayer", version = "")
@Commands({
        @org.bukkit.plugin.java.annotation.command.Command(name = "mpplay")
})
@ApiVersion(ApiVersion.Target.v1_13)
public final class Musicplayer extends JavaPlugin {

    List<Thread> stopWhen = new ArrayList<>();

    @Override
    public void onEnable() {
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        stopWhen.forEach(Thread::stop);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 ){
            sender.sendMessage("/mpplay play (wav파일 / fftdat파일) - 바로 재생");
            sender.sendMessage("/mpplay transform (wav파일) - 전처리");
        } else  {
            if (args[0].equals("play")) {
                File f = new File(args[1]);
                try {
                    MusicPlayerContext context = new MusicPlayerContext();
                    context.setMusicId(UUID.randomUUID());
                    context.setPlayer((Player) sender);
                    context.setVolumeSettings(new StaticVolumeSetting(1.0));
                    context.setProvider(new WavFileFFTFrameProvider(f));
                    context.setFile(f);
                    Thread t = new Thread(context);
                    t.start();
                    stopWhen.add(t);
                    sender.sendMessage("Playing...");
                } catch (UnsupportedAudioFileException | IOException e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed");
                }
            }
        }

        return true;
    }
}
