package kr.syeyoung.musicplayer.fft;

import org.quifft.output.FrequencyBin;

import java.io.Serializable;
import java.util.Arrays;

public class PluginFFTFrame implements Serializable {

    private static final long serialVersionUID = -7516533474790254383L;

    public double frameStartMs;
    public double frameEndMs;
    public PluginFrequencyBin[] bins;
    public PluginFFTFrame(double startMs, double endMs, PluginFrequencyBin[] bins) {
        this.frameStartMs = startMs;
        this.frameEndMs = endMs;
        this.bins = bins;
    }

    @Override
    public String toString() {
        return "FFTFrame{" +
                "frameStartMs=" + frameStartMs +
                ", frameEndMs=" + frameEndMs +
                ", bins=" + Arrays.toString(bins) +
                '}';
    }

    public PluginFFTFrame() {
    }
}
