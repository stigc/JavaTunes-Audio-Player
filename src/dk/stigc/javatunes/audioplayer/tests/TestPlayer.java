package dk.stigc.javatunes.audioplayer.tests;

import java.util.ArrayDeque;

import dk.stigc.javatunes.audioplayer.other.AbstractTrack;
import dk.stigc.javatunes.audioplayer.player.AudioInfo;
import dk.stigc.javatunes.audioplayer.player.AudioInfoInternal;
import dk.stigc.javatunes.audioplayer.player.AudioPlayer;
import dk.stigc.javatunes.audioplayer.player.IAudio;
import dk.stigc.javatunes.audioplayer.player.IAudioPlayerHook;

public class TestPlayer implements IAudioPlayerHook
{
	AudioPlayer audioPlayer;
	public ArrayDeque<String> tracks = new ArrayDeque<String>();
	public volatile boolean noMoreTracks;
	
	public TestPlayer()
	{
		audioPlayer = new AudioPlayer();
		audioPlayer.addHook(this);
	}

	public void playNextTrack()
	{
		if (tracks.size()<=0)
		{
			noMoreTracks = true;
			return;
		}
	
		try
		{
			audioPlayer.play(tracks.pop());
		} 
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void audioInterrupted(IAudio audio)
	{
	}
	
	@Override
	public void audioFailed(IAudio audio, Exception ex)
	{
		playNextTrack();
	}

	@Override
	public void audioEnded(IAudio audio)
	{
		playNextTrack();
	}

	public void start() throws Exception
	{
		audioPlayer.play(tracks.pop());
	}

	public void printInfo()
	{
		System.out.println(audioPlayer.getAudioInfo().toString());
	}

	public void stop()
	{
		audioPlayer.stop();
	}

	@Override
	public void audioStarting(AudioInfo audio)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void tagsParsed(int sourceHashCode, AbstractTrack track)
	{
		// TODO Auto-generated method stub
		
	}
}
