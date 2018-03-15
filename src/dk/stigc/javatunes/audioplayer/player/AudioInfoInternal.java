package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.*;

public class AudioInfoInternal
{
	private int channels, sampleRate, bitsPerSample;
	public int kbps;
	public int lengthInSeconds;
	public volatile long positionInMs;
	public long lengthInBytes;
	public String icyName, icyGenre, icyStreamTitle;
	public Codec codec;
	
	public synchronized void init(int channels, int sampleRate, int bitsPerSample)
	{
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
	}  
	
	private double bitrateCount, bitrateSum;
	
	public synchronized void addVariableBitrate(double bitrate)
	{
		bitrateCount++;
		bitrateSum += bitrate;
	}

	public synchronized boolean setLengthInSeconds(int lengthInSeconds)
	{
		this.lengthInSeconds = lengthInSeconds;
		
		if (lengthInBytes>0 && lengthInSeconds>0 && kbps == 0)
		{
			kbps = (int) (lengthInBytes / lengthInSeconds * 8/1000);
			return true;
		}
		
		return false;
	}
	
	private int getVariableKbps()
	{
		if (bitrateCount<1)
			return 0;
		
		int avg = (int)(bitrateSum/bitrateCount);
		avg /=1000;
		bitrateSum = 0;
		bitrateCount = 0;
		return avg;
	}

	public synchronized AudioInfo createClone()
	{
		AudioInfo ai = new AudioInfo();
		ai.channels = this.channels;
		ai.sampleRate = this.sampleRate;
		ai.bitsPerSample = this.bitsPerSample;
		ai.kbps = this.kbps;
		ai.kbpsVar = this.getVariableKbps();
		ai.lengthInSeconds = this.lengthInSeconds;
		ai.positionInMs = this.positionInMs;
		ai.lengthInBytes = this.lengthInBytes;
		ai.icyName = this.icyName;
		ai.icyGenre = this.icyGenre;
		ai.icyStreamTitle = this.icyStreamTitle;
		ai.codec = this.codec;
		return ai;
	}
}
