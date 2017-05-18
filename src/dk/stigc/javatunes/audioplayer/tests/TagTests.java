package dk.stigc.javatunes.audioplayer.tests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import dk.stigc.javatunes.audioplayer.other.Track;
import dk.stigc.javatunes.audioplayer.tagreader.TagReaderManager;

public class TagTests
{
	String root = "C:\\data\\Projekter\\Eclipse.workspace\\JavaTunes\\other\\Test audio files\\";

	@Test
	public void id3vTagsWillBeParsed() throws Exception
	{
		File file = new File(root + "MP3\\01 Steady As She Goes.id3v2.2.mp3");
		Track track = new TagReaderManager().read(file);
		System.out.println(track.toString());
	}
	@Test
	public void quicktimeTagsWillBeParsed() throws FileNotFoundException
	{
		String path = root + "AAC\\03 Down The Nightclub.m4a";
		Track track = new TagReaderManager().read(new File(path));
		System.out.println(track.toString());
	}

}
