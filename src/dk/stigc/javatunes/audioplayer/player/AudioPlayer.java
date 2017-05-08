package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.tagreader.*;

public class AudioPlayer
{
	IAudioPlayerHook hook;
	private volatile int volume;
    public volatile boolean paused;
    public AudioInfo audioInfo;
    private BasePlayer player = new VoidPlayer();
    
	public AudioPlayer()
	{
		this.setVolume(100);		
	}  
	
	public void addHook(IAudioPlayerHook hook)
	{
		this.hook = hook;
	}    

    public synchronized void setSpeed(int speed)
    {
   		player.setSpeed(speed);    		
    }

    public synchronized void pause() 
    {
   		paused = !paused;
    	player.pause();
    }

    public synchronized void setVolume (int volume) 
    {
    	if (volume<0 || volume>100)
    		throw new RuntimeException("Volume should be between 0 and 100");
    	
    	this.volume = volume;
    	double gain = volume/100.0;
   		player.setVolume(gain);
    }
    
    public synchronized void stop(boolean forced)
    {	
		player.stop(forced);

		if (paused)
			pause();
    }

	public synchronized AudioInfo play(String path) throws Exception
	{
		return play(new AudioImpl(path), false, false);
	}

	public synchronized AudioInfo play(IAudio audio) throws Exception
	{
		return play(audio, false, false);
	}
	
	public synchronized AudioInfo play(IAudio audio, boolean forced, boolean replayGainInAlbumMode) throws Exception
	{
    	stop(forced);
    	
    	audioInfo = new AudioInfo();
    	audioInfo.codec = audio.getCodec();
    	InputstreamSelector inputStreamSelector = new InputstreamSelector();
    	InputStream is = inputStreamSelector.getInputStream(audio, audioInfo);
    	
    	extractMp4ContainerCodec(audio, inputStreamSelector);
    	
    	switch (audioInfo.codec)
		{
			case flac:
				player = new FLACPlayer();
				break;
			case vorbis:
				player = new OggPlayer();
				break;
			case wavpack:
				player = new WavPackPlayer();
				break;
			case aacadts:
				player = new AacAdtsPlayer();
				break;					
			case aac:
			case mp4container:
				player = new AacMp4Player();
				break;					
			case alac:
				player = new AlacPlayer();
				break;							
			default:
				player = new MP3Player();				
		}
		
		double gain = volume/100.0;
		
		player.hook = hook;
		player.setData(is, audio, audioInfo, gain, replayGainInAlbumMode);	
		player.audioInfo.contenLength = inputStreamSelector.contentLength;
		player.audioInfo.granules = inputStreamSelector.granules;
		player.start();

		Log.write("Player started: " + player.getClass().getName());
		return audioInfo;
	}

	private void extractMp4ContainerCodec(IAudio audio,
			InputstreamSelector inputStreamSelector)
			throws FileNotFoundException
	{
    	if (audioInfo.codec == Codec.mp4container 
    			&& inputStreamSelector.isRemote == false)
    	{
    		File file = new File(audio.getPath());
    		Track track = new TagReaderManager().read(file);
    		if (track != null)
    			audioInfo.codec = track.codec;
    	}
	}
    
    public int getVolume()
    {
    	return volume;
    }
}
