package kr.syeyoung.musicplayer.data;

import org.quifft.output.FFTFrame;

public interface FFTProvider {
    boolean prepareFrame();

    FFTFrame getFrame();

    boolean isDoStopSound();
}
