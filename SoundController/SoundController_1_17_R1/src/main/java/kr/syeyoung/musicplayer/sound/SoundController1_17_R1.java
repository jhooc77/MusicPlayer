package kr.syeyoung.musicplayer.sound;

import io.netty.channel.Channel;
import net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class SoundController1_17_R1 extends SoundController {

    Channel channel;
    Vec3D vecCache;
    List<Channel> channelList;
    PlayerList playerList;

    public SoundController1_17_R1(Player player) {
        super(player);
        this.channel = ((CraftPlayer) player).getHandle().b.a.k;
        this.playerList = ((CraftPlayer) player).getHandle().c.getPlayerList();
        player.sendMessage("Sound Controller detected");
    }

    @Override
    public void writeSound(String sound, float volume, float pitch) {
        if (vecCache == null) {
            Location loc = player.getLocation();
            vecCache = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
        }
        channel.write(new PacketPlayOutCustomSoundEffect(MinecraftKey.a(sound), SoundCategory.a, vecCache, volume, pitch));
    }

    @Override
    public void writeSoundNearby(String sound, float volume, float pitch, int range) {
        if (vecCache == null) {
            Location loc = player.getLocation();
            vecCache = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
            List<Channel> channels = new ArrayList<>();
            for (EntityPlayer entityPlayer : playerList.getPlayers()) {
                double x = entityPlayer.locX() - vecCache.getZ();
                double y = entityPlayer.locY() - vecCache.getX();
                double z = entityPlayer.locZ() - vecCache.getZ();
                if (x * x + y * y + z * z < range * range) {
                    channels.add(entityPlayer.b.a.k);
                }
            }
            this.channelList = channels;
        }
        for (Channel channel1 : channelList) {
            channel1.write(new PacketPlayOutCustomSoundEffect(MinecraftKey.a(sound), SoundCategory.a, vecCache, volume, pitch));
        }
    }

    @Override
    public void flushSound() {
        vecCache = null;
        channel.flush();
        if (channelList != null) {
            for (Channel channel1 : channelList) {
                channel1.flush();
            }
        }
    }
}
