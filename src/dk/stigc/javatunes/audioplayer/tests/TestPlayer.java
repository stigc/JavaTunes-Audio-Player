package dk.stigc.javatunes.audioplayer.tests;

import java.util.ArrayDeque;

import dk.stigc.javatunes.audioplayer.player.AudioInfo;
import dk.stigc.javatunes.audioplayer.player.AudioPlayer;
import dk.stigc.javatunes.audioplayer.player.IAudio;
import dk.stigc.javatunes.audioplayer.player.IAudioPlayerHook;

public class TestPlayer implements IAudioPlayerHook
{
	AudioPlayer audioPlayer;
	public AudioInfo audioInfo;
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
			audioInfo = audioPlayer.play(tracks.pop());
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
		audioInfo = audioPlayer.play(tracks.pop());
	}

	public void printInfo()
	{
		synchronized (audioInfo)
		{
			System.out.println(audioInfo.toString());	
		}
	}

	public void stop()
	{
		audioPlayer.stop();
	}
}
