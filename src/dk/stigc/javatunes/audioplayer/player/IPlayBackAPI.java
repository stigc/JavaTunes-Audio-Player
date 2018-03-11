package dk.stigc.javatunes.audioplayer.player;

import javax.sound.sampled.LineUnavailableException;

public interface IPlayBackAPI
{
	int write(byte[] pcm, int start, int length);
	void writeToFlacOutput(byte[] pcm, int length, int bps, int rate, int channels) throws RuntimeException;
	void initAudioLine(int channels, int rate, int bps, boolean signed, double gain) throws LineUnavailableException;
}
