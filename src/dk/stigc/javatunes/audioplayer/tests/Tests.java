package dk.stigc.javatunes.audioplayer.tests;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.Mixer;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.*;
import dk.stigc.javatunes.audioplayer.tagreader.TagReaderManager;

public class Tests
{
	String root = "C:\\data\\Projekter\\Eclipse.workspace\\JavaTunes\\other\\Test audio files\\";

	@BeforeClass
	public static void setUpBeforeClass() throws Exception 
	{
	   Logger logger = Logger.getLogger("javatunes.mediaplayer");
	   logger.setUseParentHandlers(false);
	   ConsoleHandler handler = new ConsoleHandler();
	   handler.setFormatter(new LogFormatter());
	   logger.addHandler(handler);
	}
	
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
			Thread.sleep(100);
		
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
	public void twoAudioPlayers() throws Exception
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
	public void gaplessPlaybackWillWork() throws Exception
	{
		TestPlayer player = new TestPlayer();
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\01 Track01.ogg");
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\02 Track02.ogg");
		player.tracks.add(root + "gapless.test.samples\\Vorbis\\03 Track03.ogg");
		player.start();
		
		while (player.noMoreTracks == false)
		{
			player.printInfo();
			Thread.sleep(1000);
		}
	}
	
	@Test
	public void shoutCastWillWork() throws Exception
	{
		TestPlayer player = new TestPlayer();
		player.tracks.add("http://178.33.104.250:80/stream");
		player.tracks.add("http://streaming.radio24syv.dk/pls/24syv_96_IR.pls");
		player.tracks.add("http://live-icy.gss.dr.dk:8000/A/A03L.mp3.m3u");
		player.tracks.add("http://51.254.29.40:80/stream3");
		player.tracks.add("http://178.33.45.203:80/stream2");
		
		player.start();
		
		int seconds = 0;
		while (player.noMoreTracks == false)
		{
			seconds++;
			Thread.sleep(1000);
			player.printInfo();
			if (seconds % 5 == 0)
				player.playNextTrack();
		}
		
		player.stop();
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
	public void GitHubDemoTest() throws Exception
	{
		File file = new File(root + "WavPack\\8bit.wv");
		Track track = new TagReaderManager().read(file);
		write(track.toString());
		
		AudioPlayer player = new AudioPlayer();
		AudioInfo ai = player.play(track, false);
		
		while (player.isPlaying()) 
		{
			write(ai.toString());
			Thread.sleep(1000);
		}
	}
	
	public void GitHubDemoTest2() throws Exception
	{
		AudioPlayer player = new AudioPlayer();
		player.enableFlacOutput(new File("output.flac"));
		player.setOutputToMixer(false); //uncomment to disable sound in speakers 
		player.play(root + "ALAC\\08 Lilac.m4a");
		
		while (player.isPlaying()) 
			Thread.sleep(1000);

		player.finishFlacOutput();
	}
	
	@Test
	public void pauseShouldWork() throws Exception
	{
		String path = root + "MP3\\id3v2.4 UTF-8 Nanna.mp3";
		AudioPlayer audioPlayer = new AudioPlayer();
		audioPlayer.play(path);
		write("pause");
		audioPlayer.pause();
		audioPlayer.pause(); //allowed more than 1..
		Thread.sleep(2000);
		
		write("pause");
		audioPlayer.start();
		audioPlayer.start(); //allowed more than 1..
		Thread.sleep(2000);
		
		write("pause and play");
		audioPlayer.pause();
		audioPlayer.play(path);
		Thread.sleep(2000);
		Thread.sleep(2000);
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
	}

	
	
	@Test
	public void displayMixerInfo()
	{
	  Mixer.Info [] mixersInfo = AudioSystem.getMixerInfo();

	  for (Mixer.Info mixerInfo : mixersInfo)
	  {
		 write("Mixer: " + mixerInfo.getName());

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
		write("  " + lineInfo.toString());

	  if (lineInfo instanceof DataLine.Info)
	   {
	     DataLine.Info dataLineInfo = (DataLine.Info)lineInfo;

	     AudioFormat [] formats = dataLineInfo.getFormats();
	     for (final AudioFormat format : formats)
	    	 write("    " + format.toString());
	   }
	}
	
	private void playFor3Seconds(String path) throws Exception
	{
		playFor3Seconds(path, null);
	}
	
	private void playFor3Seconds(String path, Track track) throws Exception
	{
		write("Testing: " + path);
		
		AudioPlayer audioPlayer = new AudioPlayer();
		
		AudioInfo ai = track != null ?
				audioPlayer.play(track, false) : audioPlayer.play(path);
				
		long startTime = System.currentTimeMillis();
		
		while (System.currentTimeMillis() - startTime < 3000)
		{
			Common.sleep(1000);	
			synchronized (ai)
			{
				write(" * " + ai.toString());
			}
		}
		
		audioPlayer.stopAndWaitUntilPlayerThreadEnds();
	}

	private static void write(String msg)
	{
		System.out.println("      " + msg);
	}
}
