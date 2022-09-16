package kr.syeyoung.musicplayer.data;

import org.quifft.output.FFTFrame;
import org.quifft.output.FFTResult;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;

public class FFTFileFFTFrameProvider implements FFTProvider {
    public FFTFileFFTFrameProvider(File f) throws UnsupportedAudioFileException, IOException {
        throw new UnsupportedOperationException(""); // Anyone wanna implement this? I'm lazy.
    }

    private FFTResult fftResult;

    private FFTFrame preparedFftFrame;
    private int frame = 0;

    @Override
    public boolean prepareFrame() {
        if (frame >= fftResult.fftFrames.length) {
            return false;
        }
        preparedFftFrame = fftResult.fftFrames[frame++];
        return true;
    }

    public FFTFrame getFrame() {
        return preparedFftFrame;
    }

    @Override
    public boolean isDoStopSound() {
        return false;
    }
}
