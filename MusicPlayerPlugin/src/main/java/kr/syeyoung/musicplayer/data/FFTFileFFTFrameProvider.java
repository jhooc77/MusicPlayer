package kr.syeyoung.musicplayer.data;

import kr.syeyoung.musicplayer.fft.PluginFFTFrame;
import org.quifft.output.FFTFrame;
import org.quifft.output.FFTResult;
import org.quifft.output.FrequencyBin;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FFTFileFFTFrameProvider implements FFTProvider {
    public FFTFileFFTFrameProvider(File f) throws UnsupportedAudioFileException, IOException, ClassNotFoundException {
        if (f.getName().endsWith(".qft")) {
            byte[] serializedMember = Files.readAllBytes(f.toPath());
            try (ByteArrayInputStream bais = new ByteArrayInputStream(serializedMember)) {
                try (ObjectInputStream ois = new ObjectInputStream(bais)) {
                    fftResult = new FFTResult();
                    List<FFTFrame> temps = new ArrayList<>();
                    try {
                        while(true) {
                            Object o = ois.readObject();
                            if (o == null) break;
                            PluginFFTFrame fftFrame = (PluginFFTFrame) o;
                            temps.add(new FFTFrame(fftFrame.frameStartMs, fftFrame.frameEndMs, Arrays.stream(fftFrame.bins).map(b -> new FrequencyBin(b.frequency, b.amplitude)).toArray(FrequencyBin[]::new)));
                        }
                    } catch(EOFException ignored) {}
                    fftResult.fftFrames = temps.toArray(new FFTFrame[0]);
                }
            }
        } else {
            throw new UnsupportedAudioFileException("Unsupported audio file");
        }
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
}
