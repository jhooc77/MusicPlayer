package kr.syeyoung.musicplayer;

import kr.syeyoung.musicplayer.data.MusicPlayerContext;
import kr.syeyoung.musicplayer.data.StaticVolumeSetting;
import kr.syeyoung.musicplayer.data.WavFileFFTFrameProvider;
import kr.syeyoung.musicplayer.sound.SoundController;
import kr.syeyoung.musicplayer.sound.SoundControllerProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Plugin(name="MusicPlayer", version = "")
@Commands({
        @org.bukkit.plugin.java.annotation.command.Command(name = "mpplay")
})
@ApiVersion(ApiVersion.Target.v1_13)
public final class Musicplayer extends JavaPlugin {

    List<Thread> stopWhen = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        stopWhen.forEach(Thread::stop);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 ){
            sender.sendMessage("/mpplay play (wav파일) [windowsSize] [overlapPercent] [stopSoundBetweenEachNote] [useBukkitSoundController] - 바로 재생");
            sender.sendMessage("/mpplay stop - 노래 끄기");
            sender.sendMessage("/mpplay reload - 리로드");
        } else  {
            if (args[0].equals("play")) {
                try {
                    File f = new File(getDataFolder(), args[1]);
                    int size = 4096;
                    double percent = 0.5;
                    boolean divide = false;
                    SoundController controller;
                    if (args.length > 2) {
                        size = Integer.parseInt(args[2]);
                    }
                    if (args.length > 3) {
                        percent = Double.parseDouble(args[3]);
                    }
                    if (args.length > 4) {
                        divide = Boolean.parseBoolean(args[4]);
                    }
                    if (args.length > 5) {
                        if (Boolean.parseBoolean(args[5])) {
                            controller = SoundControllerProvider.bukkitController((Player) sender);
                        } else {
                            controller = SoundControllerProvider.nettyController((Player) sender);
                        }
                    } else {
                        controller = SoundControllerProvider.nettyController((Player) sender);
                    }
                    MusicPlayerContext context = new MusicPlayerContext();
                    context.setMusicId(UUID.randomUUID());
                    context.setPlayer((Player) sender);
                    context.setVolumeSettings(new StaticVolumeSetting(1.0));
                    context.setProvider(new WavFileFFTFrameProvider(f, size, percent, divide));
                    context.setFile(f);
                    context.setController(controller);
                    Thread t = new Thread(context);
                    t.start();
                    stopWhen.add(t);
                    sender.sendMessage("Playing...");
                } catch (Exception e) {
                    e.printStackTrace();
                    sender.sendMessage("Failed:" + e.getMessage());
                }
            } else if (args[0].equals("stop")) {
                stopWhen.forEach(Thread::stop);
                sender.sendMessage("Stopped");
            } else if (args[0].equals("reload")) {
                reloadConfig();
                sender.sendMessage("Reload complete");
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("play", "stop");
        } else if (args[0].equals("play")) {
            if (args.length == 2) {
                return Arrays.stream(getDataFolder().listFiles()).filter(f -> f.getName().endsWith(".wav")).map(f -> f.getName()).collect(Collectors.toList());
            } else if (args.length == 3) {
                return Arrays.asList("512", "1024", "2048", "4096", "8192");
            } else if (args.length == 4) {
                return Arrays.asList("0", "0.5", "0.99");
            } else if (args.length == 5) {
                return Arrays.asList("false", "true");
            } else if (args.length == 6) {
                return Arrays.asList("false", "true");
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
