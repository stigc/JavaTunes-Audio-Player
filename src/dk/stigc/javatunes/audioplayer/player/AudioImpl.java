package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.*;

public class AudioImpl implements IAudio
{
	String path;
	
	public AudioImpl(String path)
	{
		this.path = path;
	}
	
	@Override
	public String getPath()
	{
		return path;
	}

	@Override
	public double getReplayGain(boolean albumMode)
	{
		return 0;
	}

	@Override
	public Codec getCodec()
	{
		return Codec.extractCodecFromExtension(path);
	}

}
