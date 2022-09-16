package kr.syeyoung.musicplayer;

import kr.syeyoung.musicplayer.data.FFTFileFFTFrameProvider;
import kr.syeyoung.musicplayer.data.MusicPlayerContext;
import kr.syeyoung.musicplayer.data.StaticVolumeSetting;
import kr.syeyoung.musicplayer.data.WavFileFFTFrameProvider;
import kr.syeyoung.musicplayer.fft.PluginFFTFrame;
import kr.syeyoung.musicplayer.fft.PluginFrequencyBin;
import kr.syeyoung.musicplayer.sound.SoundController;
import kr.syeyoung.musicplayer.sound.SoundControllerProvider;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.annotation.command.Commands;
import org.bukkit.plugin.java.annotation.plugin.ApiVersion;
import org.bukkit.plugin.java.annotation.plugin.Plugin;
import org.quifft.QuiFFT;
import org.quifft.output.FFTFrame;
import org.quifft.output.FFTResult;
import org.quifft.output.FrequencyBin;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

@Plugin(name="MusicPlayer", version = "1.0-SNAPSHOT")
@Commands({
        @org.bukkit.plugin.java.annotation.command.Command(name = "mpplay", permission = "musicplayer.play")
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
            sender.sendMessage("/mpplay play (wav파일) [windowsSize] [overlapPercent] [stopSoundBetweenEachNote] [useBukkitSoundController] [debug] [resourceName] [range] - 바로 재생");
            sender.sendMessage("/mpplay save (wav파일) [windowsSize] [overlapPercent] - qft파일로 저장");
            sender.sendMessage("/mpplay load (qft파일) [stopSoundBetweenEachNote] [useBukkitSoundController] [debug] [resourceName] [range] - 바로 재생");
            sender.sendMessage("/mpplay stop - 노래 끄기");
            sender.sendMessage("/mpplay reload - 리로드");
        } else  {
            if (args[0].equals("play")) {
                getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        File f = new File(getDataFolder(), args[1]);
                        int range = -1;
                        int size = 4096;
                        double percent = 0.5;
                        boolean divide = false;
                        boolean debug = false;
                        String resourceName = "sinewaves_short_test";
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
                        if (args.length > 6) {
                            debug = Boolean.parseBoolean(args[6]);
                        }
                        if (args.length > 7) {
                            resourceName = args[7];
                        }
                        if (args.length > 8) {
                            range = Integer.parseInt(args[8]);
                        }
                        MusicPlayerContext context = new MusicPlayerContext();
                        context.setMusicId(UUID.randomUUID());
                        context.setPlayer((Player) sender);
                        context.setVolumeSettings(new StaticVolumeSetting(1.0));
                        context.setProvider(new WavFileFFTFrameProvider(f, size, percent));
                        context.setDoStopSound(divide);
                        context.setFile(f);
                        context.setController(controller);
                        context.setRange(range);
                        context.setDebug(debug);
                        List<Integer> integerList = getConfig().getIntegerList("sinewave-list");
                        if (integerList.size() == 0) {
                            context.setSinewaveRegistry(new SinewaveRegistry(resourceName));
                        } else {
                            context.setSinewaveRegistry(new SinewaveRegistry(resourceName, integerList));
                        }
                        Thread t = new Thread(context);
                        t.start();
                        stopWhen.add(t);
                        sender.sendMessage("Playing...");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage("Failed:" + e.getMessage());
                    }
                });
            } else if (args[0].equals("stop")) {
                stopWhen.forEach(Thread::stop);
                sender.sendMessage("Stopped");
            } else if (args[0].equals("reload")) {
                reloadConfig();
                sender.sendMessage("Reload complete");
            } else if (args[0].equals("save")) {

                getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        sender.sendMessage("Saving...");
                        File f = new File(getDataFolder(), args[1]);
                        int size = 4096;
                        double percent = 0.5;
                        if (args.length > 2) {
                            size = Integer.parseInt(args[2]);
                        }
                        if (args.length > 3) {
                            percent = Double.parseDouble(args[3]);
                        }
                        FFTResult result = new QuiFFT(f)
                                .dBScale(false)
                                .normalized(true)
                                .windowSize(size)
                                .windowOverlap(percent)
                                .fullFFT();
                        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                            try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                                for (FFTFrame fftFrame : result.fftFrames) {
                                    PluginFrequencyBin[] bins = new PluginFrequencyBin[fftFrame.bins.length];
                                    int j = 0;
                                    for (FrequencyBin bin : fftFrame.bins) {
                                        bins[j++] = new PluginFrequencyBin(bin.frequency, bin.amplitude);
                                    }
                                    oos.writeObject(new PluginFFTFrame(fftFrame.frameStartMs, fftFrame.frameEndMs, bins));
                                }
                                // serializedMember -> 직렬화된 객체
                                byte[] serializedMember = baos.toByteArray();
                                sender.sendMessage(serializedMember.length + " bytes");
                                Files.write(getDataFolder().toPath().resolve(f.getName().substring(0, f.getName().length()-4) + "_"  + size + "_" + percent + ".qft"), serializedMember);
                            }
                        }
                        sender.sendMessage("Save Complete");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage("Failed:" + e.getMessage());
                    }
                });
            } else if (args[0].equals("load")) {
                getServer().getScheduler().runTaskAsynchronously(this, () -> {
                    try {
                        File f = new File(getDataFolder(), args[1]);
                        int range = -1;
                        boolean divide = false;
                        boolean debug = false;
                        String resourceName = "sinewaves_short_test";
                        SoundController controller;
                        if (args.length > 2) {
                            divide = Boolean.parseBoolean(args[2]);
                        }
                        if (args.length > 3) {
                            if (Boolean.parseBoolean(args[3])) {
                                controller = SoundControllerProvider.bukkitController((Player) sender);
                            } else {
                                controller = SoundControllerProvider.nettyController((Player) sender);
                            }
                        } else {
                            controller = SoundControllerProvider.nettyController((Player) sender);
                        }
                        if (args.length > 4) {
                            debug = Boolean.parseBoolean(args[4]);
                        }
                        if (args.length > 5) {
                            resourceName = args[5];
                        }
                        if (args.length > 6) {
                            range = Integer.parseInt(args[6]);
                        }
                        MusicPlayerContext context = new MusicPlayerContext();
                        context.setMusicId(UUID.randomUUID());
                        context.setPlayer((Player) sender);
                        context.setVolumeSettings(new StaticVolumeSetting(1.0));
                        context.setProvider(new FFTFileFFTFrameProvider(f));
                        context.setFile(f);
                        context.setDoStopSound(divide);
                        context.setController(controller);
                        context.setRange(range);
                        context.setDebug(debug);
                        List<Integer> integerList = getConfig().getIntegerList("sinewave-list");
                        if (integerList.size() == 0) {
                            context.setSinewaveRegistry(new SinewaveRegistry(resourceName));
                        } else {
                            context.setSinewaveRegistry(new SinewaveRegistry(resourceName, integerList));
                        }
                        Thread t = new Thread(context);
                        t.start();
                        stopWhen.add(t);
                        sender.sendMessage("Playing...");
                    } catch (Exception e) {
                        e.printStackTrace();
                        sender.sendMessage("Failed:" + e.getMessage());
                    }
                });
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("play", "stop", "save", "load");
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
            } else if (args.length == 7) {
                return Arrays.asList("false", "true");
            } else if (args.length == 8) {
                return Arrays.asList("sinewaves",
                        "sinewaves_short",
                        "sinewaves_short_0.1",
                        "sinewaves_short_test",
                        "sinewaves_shade_50",
                        "sinewaves_shade_50_loud",
                        "sinewaves_shade_100",
                        "sinewaves_shade_100_loud");
            } else if (args.length == 9) {
                return Collections.singletonList("16");
            }
        } else if (args[0].equals("save")) {
            if (args.length == 2) {
                return Arrays.stream(getDataFolder().listFiles()).filter(f -> f.getName().endsWith(".wav")).map(f -> f.getName()).collect(Collectors.toList());
            } else if (args.length == 3) {
                return Arrays.asList("512", "1024", "2048", "4096", "8192");
            } else if (args.length == 4) {
                return Arrays.asList("0", "0.5", "0.99");
            }
        } else if (args[0].equals("load")) {
            if (args.length == 2) {
                return Arrays.stream(getDataFolder().listFiles()).filter(f -> f.getName().endsWith(".qft")).map(f -> f.getName()).collect(Collectors.toList());
            } else if (args.length == 3) {
                return Arrays.asList("false", "true");
            } else if (args.length == 4) {
                return Arrays.asList("false", "true");
            } else if (args.length == 5) {
                return Arrays.asList("false", "true");
            } else if (args.length == 6) {
                return Arrays.asList("sinewaves",
                        "sinewaves_short",
                        "sinewaves_short_0.1",
                        "sinewaves_short_test",
                        "sinewaves_shade_50",
                        "sinewaves_shade_50_loud",
                        "sinewaves_shade_100",
                        "sinewaves_shade_100_loud");
            } else if (args.length == 7) {
                return Collections.singletonList("16");
            }
        }
        return super.onTabComplete(sender, command, alias, args);
    }
}
