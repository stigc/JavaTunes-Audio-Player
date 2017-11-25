package dk.stigc.javatunes.audioplayer.tests;
import static org.junit.Assert.*;

import java.io.File;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.*;

public class Tests
{
	static 
	{
	   Logger logger = Logger.getLogger("javatunes.mediaplayer");
	   logger.setUseParentHandlers(false);
	   ConsoleHandler handler = new ConsoleHandler();
	   handler.setFormatter(new LogFormatter());
	   logger.addHandler(handler);
	}
	
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
	public void decoderShouldReportError() throws Exception
	{
		Track track = new Track();
		track.path = "lincense.txt";
		track.codec = Codec.mp3;
		
		final List<String> list = Collections.synchronizedList(new ArrayList<String>());

		final AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.addHook(new IAudioPlayerHook() {
			@Override
			public void audioInterrupted(IAudio audio)
			{
				list.add("audioInterrupted");
			}
			@Override
			public void audioFailed(IAudio audio, Exception ex)
			{
				list.add(ex.getMessage());	
				
			}
			@Override
			public void audioEnded(IAudio audio)
			{
				list.add("audioEnded");
				
			}
		});
		
		audioPlayer.play(track);
		
		while(list.size()==0)
			Thread.sleep(50);
		
		Thread.sleep(1000);
		
		assertEquals(1, list.size());
		assertTrue(list.contains("Missing mp3 header"));
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
	public void aacAdtsWillPlay() throws Exception
	{
		playFor3Seconds(root + "AAC\\dr.aac");
	}
	
	@Test
	public void TwoAudioPlayers() throws Exception
	{
		AudioPlayer ap1 = new AudioPlayer();
		ap1.play(root + "AAC\\03 Down The Nightclub.m4a");
		
		AudioPlayer ap2 = new AudioPlayer();
		ap2.play(root + "AAC\\dr.aac");
		
		Thread.sleep(5000);
		
		ap1.stop();
		ap2.stop();
	}

	@Test
	public void AudioPlayerWithFlacEncoder() throws Exception
	{
		final ArrayDeque<String> tracks = new ArrayDeque<String>();
		tracks.add(root + "gapless.test.samples\\Vorbis\\01 Track01.ogg");
		tracks.add(root + "gapless.test.samples\\Vorbis\\02 Track02.ogg");
		tracks.add("http://178.33.104.250:80/stream");
		
		final AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.addHook(new IAudioPlayerHook() {
			@Override
			public void audioInterrupted(IAudio audio)
			{
				// TODO Auto-generated method stub
			}
			@Override
			public void audioFailed(IAudio audio, Exception ex)
			{
				// TODO Auto-generated method stub
			}
			@Override
			public void audioEnded(IAudio audio)
			{
				if (tracks.size()>0)
				{
					System.out.println("Next");						
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
			}
		});
		
		audioPlayer.enableFalcOutput(new File("new-flac-output.flac"));
		audioPlayer.play(tracks.pop());
		Thread.sleep(15000);
		
		audioPlayer.stopFlacOutput();
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
		
	}
	
	
	@Test
	public void globalReplayGain() throws Exception
	{
		BasePlayer.setGlobalRpgain(-20);
		playFor3Seconds(root + "AAC\\03 Down The Nightclub.m4a");
		
		BasePlayer.setGlobalRpgain(5);
		playFor3Seconds(root + "AAC\\03 Down The Nightclub.m4a");
		
		BasePlayer.setGlobalRpgain(0);
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
		playFor3Seconds("http://178.33.104.250:80/stream");
	}
	

	@Test
	public void bps24WillWork() throws Exception
	{
		playFor3Seconds(root + "FLAC\\24bps-96khz.01 999,999.flac");
	}
	
	@Test
	public void bps8WillWork() throws Exception
	{
		playFor3Seconds(root + "WavPack\\8bit.wv");
	}
	
	@Test
	public void tracksCanBeChanged() throws Exception
	{
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(root + "WavPack\\8bit.wv");
		Thread.sleep(1000);
		
		audioPlayer.play(root + "AAC\\03 Down The Nightclub.m4a");
		Thread.sleep(1000);
		
		audioPlayer.stop();
	}
	
	
	@Test
	public void pauseShouldWork() throws Exception
	{
		String path = root + "MP3\\id3v2.4 UTF-8 Nanna.mp3";
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(path);
		System.out.println("pause");
		audioPlayer.pause();
		audioPlayer.pause(); //allowed more than 1..
		Thread.sleep(2000);
		
		System.out.println("pause");
		audioPlayer.start();
		audioPlayer.start(); //allowed more than 1..
		Thread.sleep(2000);
		
		System.out.println("pause and play");
		audioPlayer.pause();
		audioPlayer.play(path);
		Thread.sleep(2000);
		Thread.sleep(2000);
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
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
			public void audioInterrupted(IAudio audio)
			{
				// TODO Auto-generated method stub
			}
			@Override
			public void audioFailed(IAudio audio, Exception ex)
			{
				// TODO Auto-generated method stub
			}
			@Override
			public void audioEnded(IAudio audio)
			{
				if (tracks.size()>0)
				{
					System.out.println("Next");						
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
			}
		});
		
		audioPlayer.play(tracks.pop());
		Thread.sleep(15000);
	}
	
	@Test
	public void displayMixerInfo()
	{
	  Mixer.Info [] mixersInfo = AudioSystem.getMixerInfo();

	  for (Mixer.Info mixerInfo : mixersInfo)
	   {
	     System.out.println("Mixer: " + mixerInfo.getName());

	     Mixer mixer = AudioSystem.getMixer(mixerInfo);

	     Line.Info [] sourceLineInfo = mixer.getSourceLineInfo();
	     for (Line.Info info : sourceLineInfo)
	       showLineInfo(info);

	     Line.Info [] targetLineInfo = mixer.getTargetLineInfo();
	     for (Line.Info info : targetLineInfo)
	       showLineInfo(info);
	   }
	}


	private static void showLineInfo(final Line.Info lineInfo)
	{
	  System.out.println("  " + lineInfo.toString());

	  if (lineInfo instanceof DataLine.Info)
	   {
	     DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;

	     AudioFormat [] formats = dataLineInfo.getFormats();
	     for (final AudioFormat format : formats)
	       System.out.println("    " + format.toString());
	   }
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
				audioPlayer.play(track, false) : audioPlayer.play(path);
				
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < 3000)
		{
			Common.sleep(1000);	
			synchronized (ai)
			{
				System.out.println(" * " + ai.toString());
			}
		}
		
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
	}
}
