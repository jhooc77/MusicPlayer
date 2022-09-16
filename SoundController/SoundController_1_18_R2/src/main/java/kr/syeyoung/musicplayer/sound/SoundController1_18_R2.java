package kr.syeyoung.musicplayer.sound;

import io.netty.channel.Channel;
import net.minecraft.network.protocol.game.PacketPlayOutCustomSoundEffect;
import net.minecraft.resources.MinecraftKey;
import net.minecraft.sounds.SoundCategory;
import net.minecraft.world.phys.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

class SoundController1_18_R2 extends SoundController {

    Channel channel;
    Vec3D vecCache;

    public SoundController1_18_R2(Player player) {
        super(player);
        this.channel = ((CraftPlayer) player).getHandle().b.a.m;
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
    public void flushSound() {
        vecCache = null;
        channel.flush();
    }
}
