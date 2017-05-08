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
		return play(new AudioImpl(path), true, false);
	}
	
	public synchronized AudioInfo play(IAudio audio, boolean forced, boolean albumMode) throws Exception
	{
    	stop(forced);
    	
    	audioInfo = new AudioInfo();
    	audioInfo.Codec = audio.getCodec();
    	InputstreamSelector inputStreamSelector = new InputstreamSelector();
    	InputStream is = inputStreamSelector.getInputStream(audio, audioInfo);
    	
    	//if only mp4 container detected use tag-parser to find codec.
    	if (audioInfo.Codec == Codec.mp4container 
    			&& inputStreamSelector.isRemote == false)
    	{
    		File file = new File(audio.getPath());
    		Track s = new TagReaderManager().read(file);
    		audioInfo.Codec = s.codec;
    	}
    	
    	switch (audioInfo.Codec)
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
		player.setData(is, audio, audioInfo, gain, albumMode);	
		player.audioInfo.contenLength = inputStreamSelector.contentLength;
		player.audioInfo.granules = inputStreamSelector.granules;
		player.start();

		Log.write("Player started: " + player.getClass().getName());
		return audioInfo;
	}
    
    public int getVolume()
    {
    	return volume;
    }
}
