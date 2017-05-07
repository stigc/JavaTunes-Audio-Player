package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import com.sun.xml.internal.bind.v2.model.annotation.Quick;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.tagreader.*;

public class AudioPlayer
{
	private volatile int volume;
    public volatile boolean paused = false;
    public volatile boolean playing = false;
    public AudioInfo audioInfo;
    private volatile BasePlayer player = new VoidPlayer();
    
	public AudioPlayer()
	{
		this.setVolume(100);		
	}    
	
    public synchronized void stop(boolean forced)
    {	
		player.stop(forced);
		paused = false;
		playing = false;
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
		player.setData(is, audio, audioInfo, gain, albumMode);	
		
		player.audioInfo.contenLength = inputStreamSelector.contentLength;
		player.audioInfo.granules = inputStreamSelector.granules;
		

		
		player.start();

		Log.write("Player initialized: " + player.getClass().getName());
		playing = true;
		
		return audioInfo;
	}
		
	
    public void setSpeed(int speed)
    {
   		player.setSpeed(speed);    		
    }
    
    public void pause() 
    {
   		paused = !paused;
    	player.pause();
    }
    
    public int getVolume()
    {
    	return volume;
    }
    
    public void setVolume (int volume) 
    {
    	this.volume = volume;
    	double gain = volume/100.0;
   		player.setVolume(gain);
    }
}
