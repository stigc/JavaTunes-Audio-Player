package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.streams.InputStreamImpl;
import dk.stigc.javatunes.audioplayer.tagreader.*;

public class AudioPlayer
{
	private IAudioPlayerHook hook;
	private volatile int volume;
    private BasePlayer player = new VoidPlayer();
    private SourceDataLineManager dlm = new SourceDataLineManager();
    public boolean readTagsFromRemoteStreams = false;
    
	public AudioPlayer()
	{
		this.setVolume(100);		
	}  
	
	/**
	 * Thread Safe audio info access
	 * @return
	 */
	public AudioInfo getAudioInfo()
	{
		if (player.audioInfo == null)
			return null;
		
		return player.audioInfo.createClone();
	}
	
	public IAudioPlayerHook addHook(IAudioPlayerHook hook)
	{
		this.hook = hook;
		return hook;
	}    

    public synchronized void pause() 
    {
   		dlm.pause();
    }
    
    /**
     * Use this to continue after paused
     */
    public synchronized void start() 
    {
   		dlm.start();
    }
    
    public synchronized void setVolume (int volume) 
    {
    	if (volume<0 || volume>100)
    		throw new RuntimeException("Volume should be between 0 and 100");
    	
    	this.volume = volume;
    	double gain = volume/100.0;
    	dlm.setVolume(gain);
    }
    
    public synchronized void stop()
    {	
    	Log.write("Stopping");
    	
    	player.stopThread();
    	
   		dlm.discardDataInLine();
    }
   
    public synchronized void stopAndWaitUntilPlayerThreadEnds() throws InterruptedException
    {	
    	stop();

    	player.join();
    }
    
    public boolean isPlaying()
    {
    	return !player.ended;
    }

    public boolean isPaused()
    {
    	return dlm.isPaused();
    }
    
	public synchronized void play(String path) throws Exception
	{
		play(new AudioImpl(path), false);
	}

	public synchronized void play(IAudio audio) throws Exception
	{
		play(audio, false);
	}
	
	public synchronized void play(IAudio audio, boolean isAlbumMode) throws Exception
	{
    	if (player.ended == false)
    		stop();

    	AudioInfoInternal audioInfo = new AudioInfoInternal(audio);
    	
    	InputSelector inputStreamSelector = new InputSelector();
    	InputStream is = inputStreamSelector.getInputStream(audio, audioInfo);
		audioInfo.setLengthInBytes(inputStreamSelector.contentLength);

		//is.skip(1024*5*188*51);
		
		if (readTagsFromRemoteStreams && is instanceof InputStreamImpl)
		{
			AbstractTrack track = BufferTagReader.Parse(((InputStreamImpl)is).getBuffer());
			Log.write("Tag parsed: " + track);
			if (hook != null)
				hook.tagsParsed(audio.hashCode(), track);
		}
		
    	switch (audioInfo.getCodec())
		{
			case flac:
				player = new FLACPlayer();
				break;
			case vorbis:
			case oggcontainer:				
				player = new OggPlayer(inputStreamSelector.granules);
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
			case opus:
				player = new OpusPlayer(inputStreamSelector.granules);
				break;
			case hlc:
				player = new AacAdtsTsPlayer();
				break;
			default:
				player = new MP3Player();				
		}
	
    	double gain = volume/100.0;
		
		player.hook = hook;
		player.playBackApi = dlm;
		player.initialize(is, audio, audioInfo, gain, isAlbumMode, inputStreamSelector.contentLength);	
		player.start();

		dlm.start();
		
		Log.write(player.getClass().getSimpleName() + " -> " + audio.getPath());
	}

    public int getVolume()
    {
    	return volume;
    }

	public void waitUntilCurrentAudioHasEnded() throws InterruptedException
	{
		synchronized(player)
		{
			if (player.ended)
				return;
			player.wait();
		}
	}
	
	public boolean flacOutputIsEnabled()
	{
		return dlm.flacOutputIsEnabled();
	}
	
	public void enableFlacOutput(OutputStream os) throws IOException
	{
		dlm.enableFlacOutput(null, os);
	}
	
	public void enableFlacOutput(File file) throws IOException
	{
		dlm.enableFlacOutput(file, null);
	}
	
	public void finishFlacOutput() throws IOException
	{
		dlm.finishFlacOutput();
	}

	public void setOutputToMixer(boolean value)
	{
		dlm.setOutputToMixer(value);
	}
	
	public void setBufferSize(int sizeInKb)
	{
		dlm.setBufferSize(sizeInKb);
	}
}
