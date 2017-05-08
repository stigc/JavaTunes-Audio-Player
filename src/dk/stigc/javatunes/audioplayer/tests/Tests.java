package dk.stigc.javatunes.audioplayer.tests;
import static org.junit.Assert.*;

import java.io.File;
import java.io.InputStream;
import java.util.*;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.*;
import dk.stigc.javatunes.audioplayer.tagreader.*;

public class Tests
{
	String root = "C:\\data\\Projekter\\Eclipse.workspace\\JavaTunes\\other\\Test audio files\\";
	
	@Rule
	public ExpectedException thrown = ExpectedException.none();
	
	@Test
	public void whenPathDoesNotExists() throws Exception
	{
	    thrown.expect(Exception.class);
	    thrown.expectMessage("not.found does not exists");
		new AudioPlayer().play("not.found");
	}

	@Test
	public void alacWillPlay() throws Exception
	{
		playFor3Seconds(root + "ALAC\\08 Lilac.m4a");
	}

	@Test
	public void vorbisWillPlay() throws Exception
	{
		playFor3Seconds(root + "Vorbis\\Abba-Chiquitta.ogg");
	}
	
	@Test
	public void flacWillPlay() throws Exception
	{
		playFor3Seconds(root + "FLAC\\07 Det är en nåd.flac");
	}
	
	@Test
	public void wavPackWillPlay() throws Exception
	{
		playFor3Seconds(root + "WavPack\\Track01.wv");
	}
	
	@Test
	public void aacWillPlay() throws Exception
	{
		playFor3Seconds(root + "AAC\\03 Down The Nightclub.m4a");
	}
	
	@Test
	public void mp3WillPlay() throws Exception
	{
		playFor3Seconds(root + "MP3\\id3v2.4 UTF-8 Nanna.mp3");
	}
	@Test
	public void shoutCastWillPlay() throws Exception
	{
		playFor3Seconds("http://54.202.122.200:8000");
	}

	@Test
	public void tagReaderWillParseTags() throws Exception
	{
		File file = new File(root + "MP3\\01 Steady As She Goes.id3v2.2.mp3");
		Track track = new TagReaderManager().read(file);
		System.out.println(track.toString());
		
		playFor3Seconds(null, track);
	}

	@Test
	public void pauseShouldWork() throws Exception
	{
		String path = root + "MP3\\id3v2.4 UTF-8 Nanna.mp3";
		AudioPlayer audioPlayer = new AudioPlayer();
		
		audioPlayer.play(path);
		System.out.println("pause");
		audioPlayer.pause();
		Thread.sleep(2000);
		
		System.out.println("pause");
		audioPlayer.pause();
		Thread.sleep(2000);
		
		System.out.println("pause and play");
		audioPlayer.pause();
		audioPlayer.play(path);
		Thread.sleep(2000);
	}

	@Test
	public void hookWillWorkAndThisWillPlayGapless() throws Exception
	{
		final ArrayDeque<String> tracks = new ArrayDeque<String>();
		tracks.add(root + "gapless.test.samples\\Vorbis\\01 Track01.ogg");
		tracks.add(root + "gapless.test.samples\\Vorbis\\02 Track02.ogg");
		tracks.add(root + "gapless.test.samples\\Vorbis\\03 Track03.ogg");
		
		final AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.addHook(new IAudioPlayerHook() {
			@Override
			public void trackEnded(boolean finished)
			{
				try
				{
					if (tracks.size()>0)
					{
						System.out.println("Next");						
						audioPlayer.play(tracks.pop());
					}
				} 
				catch (Exception e) {}
			}
			@Override
			public void trackDecodingError(Exception ex)
			{
				System.out.println("trackDecodingError: " + ex.getMessage());					
			}
		});
		
		audioPlayer.play(tracks.pop());
		Thread.sleep(15000);
	}
	private void playFor3Seconds(String path) throws Exception
	{
		playFor3Seconds(path, null);
	}
	
	private void playFor3Seconds(String path, Track track) throws Exception
	{
		System.out.println("Testing: " + path);
		
		AudioPlayer audioPlayer = new AudioPlayer();
		
		AudioInfo ai = track != null ?
				audioPlayer.play(track, true, false) : audioPlayer.play(path);
				
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < 3000)
		{
			Common.sleep(1000);	
			synchronized (ai)
			{
				System.out.println(" * " + ai.toString());
			}
		}
		
		audioPlayer.stop(true);
	}
}
