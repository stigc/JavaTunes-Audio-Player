package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.*;

public class AudioInfoInternal
{
	private Codec codec;
	private int sourceHashCode;
	private int channels, sampleRate, bitsPerSample;
	private int lengthInSeconds;
	private long positionInMs;
	private long lengthInBytes;
	private int kbps;
	private int icyMetaInt;
	private String icyName, icyGenre, icyStreamTitle;
	private String newLocation;
	
	public AudioInfoInternal(IAudio audio)
	{
		Codec codec = audio.getCodec();
		if (codec == null || codec == Codec.unknown)
			codec = Codec.extractCodecFromExtension(audio.getPath());
		this.codec = codec;
		this.sourceHashCode = audio.hashCode();
	}

	public synchronized void init(int channels, int sampleRate, int bitsPerSample)
	{
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.bitsPerSample = bitsPerSample;
	}  
	
	public synchronized void setLengthInBytes(long lengthInBytes)
	{
		this.lengthInBytes = lengthInBytes;
	}
	
	public synchronized void setPositionInMs(long positionInMs)
	{
		this.positionInMs = positionInMs;
	}
	
	public synchronized void setKbps(int kbps)
	{
		this.kbps = kbps;
	}

	public synchronized void setIcyData(int icyMetaInt, String icyName, String icyGenre)
	{
		this.icyMetaInt = icyMetaInt;
		this.icyName = icyName;
		this.icyGenre = icyGenre;
	}
	
	public synchronized void setIcyTitle(String title)
	{
		this.icyStreamTitle = title;
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
		ai.icyMetaInt = this.icyMetaInt;
		ai.icyName = this.icyName;
		ai.icyGenre = this.icyGenre;
		ai.icyStreamTitle = this.icyStreamTitle;
		ai.codec = this.codec;
		ai.sourceHashCode = this.sourceHashCode;
		ai.newLocation = this.newLocation;
		return ai;
	}

	public synchronized boolean isCodec(Codec... codecs)
	{
		for(Codec c: codecs)
			if (c == codec)
				return true;
		return false;
	}
	
	public synchronized Codec getCodec()
	{
		return codec;
	}

	public synchronized void setCodec(Codec codec)
	{
		this.codec = codec;
	}
	
	public synchronized String getNewLocation()
	{
		return newLocation;
	}

	public synchronized void setNewLocation(String newLocation)
	{
		this.newLocation =newLocation;
	}
}
