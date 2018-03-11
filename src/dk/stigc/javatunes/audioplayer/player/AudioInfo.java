package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.*;

public class AudioInfo
{
	
	public int channels, sampleRate, bitsPerSample;
	public int kbps;
	public int granules;
	public int lengthInSeconds;
	public long positionInMs;
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
	
	public synchronized int getVariableKbps()
	{
		if (bitrateCount<1)
			return 0;
		
		int avg = (int)(bitrateSum/bitrateCount);
		avg /=1000;
		bitrateSum = 0;
		bitrateCount = 0;
		return avg;
	}

	@Override
	public String toString()
	{
		String s = "lengthInSeconds=" + lengthInSeconds;
		s += ", channels=" + channels;
		s += ", sampleRate=" + sampleRate;
		s += ", bitsPerSample=" + bitsPerSample;
		s += ", kbps=" + kbps;
		s += ", positionInMs=" + positionInMs;
		int tmp = getVariableKbps();
		if (tmp > 0)
			s += ", kbpsVaraible=" + tmp;
		s += ", lengthInBytes=" + lengthInBytes;
		s += ", codec=" + codec;
		
		if (icyName != null)
			s += ", icyName=" + icyName;
		if (icyGenre != null)
			s += ", icyGenre=" + icyGenre;
		if (icyStreamTitle != null)
			s += ", icyNowPlaying=" + icyStreamTitle;

		 return s;
	}
}
