package com.example.annac.turtleai;

import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;

import java.io.InputStream;

// można wykorzystać też inne metody na tworzenie spektrogramów
public class Spectrogram {

    // TODO: pobawić się parametrami, żeby uzyskać spektrogramy odpowiednich rozmiarów
    private final static int FFT_SAMPLE_SIZE = 1024;
    private final static int OVERLAP_FACTOR = 30;

    public static double[][] getSpectrogram(InputStream inputStream) {
        return getSpectrogram(inputStream, FFT_SAMPLE_SIZE, OVERLAP_FACTOR);
    }

    public static double[][] getSpectrogram(InputStream inputStream, int fftSampleSize, int overlapFactor){
        com.musicg.wave.extension.Spectrogram spectrogram = getSpectrogramObject(inputStream, fftSampleSize, overlapFactor);
        return spectrogram.getNormalizedSpectrogramData();
    }

    private static com.musicg.wave.extension.Spectrogram getSpectrogramObject(InputStream inputStream, int fftSampleSize, int overlapFactor){
        Wave wave = new Wave(inputStream);
        return new com.musicg.wave.extension.Spectrogram(wave, fftSampleSize, overlapFactor);
    }

    private static com.musicg.wave.extension.Spectrogram getSpectrogramObject(InputStream inputStream){
        return getSpectrogramObject(inputStream, FFT_SAMPLE_SIZE, OVERLAP_FACTOR);
    }

//    public static void showSpectrogram(inte, String outputFilename){
//        com.musicg.wave.extension.Spectrogram spectrogram = getSpectrogramObject(audioFilename);
//        GraphicRender renderer = new GraphicRender();
//        renderer.renderSpectrogram(spectrogram, outputFilename);
//    }
//
//    public static void showSpectrogram(String audioFilename , String outputFilename, int fftSampleSize, int overlapFactor){
//        com.musicg.wave.extension.Spectrogram spectrogram = getSpectrogramObject(audioFilename, fftSampleSize, overlapFactor);
//        GraphicRender renderer = new GraphicRender();
//        renderer.renderSpectrogram(spectrogram, outputFilename);
//    }

}


