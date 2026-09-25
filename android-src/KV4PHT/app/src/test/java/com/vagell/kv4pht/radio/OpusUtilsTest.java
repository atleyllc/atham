package com.vagell.kv4pht.radio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class OpusUtilsTest {
    @Test
    public void roundTripProducesAudibleSamplesAtPublicFirmwareRate() {
        int sampleRate = 48000;
        int frameSize = 1920;
        float[] pcm = new float[frameSize];
        for (int i = 0; i < frameSize; i++) {
            pcm[i] = (float) Math.sin(2.0 * Math.PI * 1000.0 * i / sampleRate) * 0.4f;
        }
        byte[] encoded = new byte[400];
        OpusUtils.OpusEncoderWrapper encoder =
                new OpusUtils.OpusEncoderWrapper(sampleRate, frameSize);
        int encodedLen = encoder.encode(pcm, encoded);
        assertTrue(encodedLen > 0);

        float[] decoded = new float[frameSize];
        OpusUtils.OpusDecoderWrapper decoder =
                new OpusUtils.OpusDecoderWrapper(sampleRate, frameSize);
        int decodedLen = decoder.decode(encoded, 0, encodedLen, decoded);
        assertEquals(frameSize, decodedLen);

        double energy = 0;
        for (int i = frameSize / 2; i < frameSize; i++) {
            energy += decoded[i] * decoded[i];
        }
        assertTrue(energy > 1.0);
    }
}
