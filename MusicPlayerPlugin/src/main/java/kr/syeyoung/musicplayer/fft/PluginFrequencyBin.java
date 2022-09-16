package kr.syeyoung.musicplayer.fft;

import java.io.Serializable;

public class PluginFrequencyBin implements Serializable {

    private static final long serialVersionUID = 1881952434688202164L;

    public double frequency;
    public double amplitude;

    public PluginFrequencyBin(double freq, double amp) {
        this.frequency = freq;
        this.amplitude = amp;
    }

    @Override
    public String toString() {
        return "FrequencyBin{" +
                "frequency=" + frequency +
                ", amplitude=" + amplitude +
                '}';
    }

    public PluginFrequencyBin() {
    }
}
