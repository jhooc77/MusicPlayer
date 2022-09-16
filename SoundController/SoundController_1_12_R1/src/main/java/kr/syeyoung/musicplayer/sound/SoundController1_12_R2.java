package kr.syeyoung.musicplayer.sound;

import io.netty.channel.Channel;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

class SoundController1_12_R2 extends SoundController {

    Channel channel;
    Vec3D vecCache;
    List<Channel> channelList;
    PlayerList playerList;

    public SoundController1_12_R2(Player player) {
        super(player);
        this.channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        this.playerList = ((CraftPlayer) player).getHandle().server.getPlayerList();
        player.sendMessage("Sound Controller detected");
    }

    @Override
    public void writeSound(String sound, float volume, float pitch) {
        if (vecCache == null) {
            Location loc = player.getLocation();
            vecCache = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
        }
        channel.write(new PacketPlayOutCustomSoundEffect(sound, SoundCategory.MASTER, vecCache.x, vecCache.y, vecCache.z, volume, pitch));
    }

    @Override
    public void writeSoundNearby(String sound, float volume, float pitch, int range) {
        if (vecCache == null) {
            Location loc = player.getLocation();
            vecCache = new Vec3D(loc.getX(), loc.getY(), loc.getZ());
            List<Channel> channels = new ArrayList<>();
            for (EntityPlayer entityPlayer : playerList.players) {
                double x = entityPlayer.locX - vecCache.x;
                double y = entityPlayer.locY - vecCache.y;
                double z = entityPlayer.locZ - vecCache.z;
                if (x * x + y * y + z * z < range * range) {
                    channels.add(entityPlayer.playerConnection.networkManager.channel);
                }
            }
            this.channelList = channels;
        }
        for (Channel channel1 : channelList) {
            channel1.write(new PacketPlayOutCustomSoundEffect(sound, SoundCategory.MASTER, vecCache.x, vecCache.y, vecCache.z, volume, pitch));
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
