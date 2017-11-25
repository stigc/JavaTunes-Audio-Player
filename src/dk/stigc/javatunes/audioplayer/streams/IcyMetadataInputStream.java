package dk.stigc.javatunes.audioplayer.streams;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.AudioInfo;

//http://jicyshout.sourceforge.net/oreilly-article/java-streaming-mp3-pt2/java-streaming-mp3-pt2.html
//http://sphere.sourceforge.net/flik/docs/streaming.html
//http://www.smackfu.com/stuff/programming/shoutcast.html
public class IcyMetadataInputStream extends InputStream
{
	private AudioInfo audioInfo;
	private int metaInt = -1;
	private int bytesUntilNextMetadata = -1;
	InputStream is;
	
	public IcyMetadataInputStream(InputStream is, AudioInfo audioInfo, int metaInt)
	{
		this.is = is;
		this.audioInfo = audioInfo;
		this.metaInt = metaInt;
		this.bytesUntilNextMetadata = metaInt;		
	}
	
	private String parseIcyStreamTitle(String v)
	{
		v = v.trim();
		if (StringFunc.startsWithIgnoreCase(v, "streamtitle='"))
		{
			v = v.substring(13).trim();
			int end = v.indexOf("';");
			if (end>-1)
				v = v.substring(0, end);
			if (StringFunc.startsWithIgnoreCase(v, "Senest spillet:"))
				v = v.substring(15).trim();
		}
		
		return v;
	}
	
	private void readIcyMetadata() throws IOException
	{
		int blocks = is.read();
		//Log.write("readIcyMetadata: " + blocks);
					
		if (blocks>0)
		{
			int bytes = blocks*16;
			byte[] data = new byte[bytes];
			int bytesRead = InputStreamHelper.readToArray(is, data);
			if (bytesRead < bytes)
				throw new IOException("Unexpected EOF while reading IcyMetadata");
			String metaData = new String(data);
			Log.write("metaData: " + metaData);	
			
			String nowPlaying = parseIcyStreamTitle(metaData);
			
			trySetNowPlaying(nowPlaying);
		}
	}	

	public void trySetNowPlaying(String nowPlaying)
	{
		if (StringFunc.isNullOrEmpty(nowPlaying))
			return;
		
		nowPlaying = nowPlaying.trim();
		
		//Sometimes it is only a "-"
		if (nowPlaying.length()>1)
		{
			synchronized(audioInfo)
			{
				audioInfo.icyStreamTitle = nowPlaying;
			}
		}
	}
	
	int reads = 0;
	public int read() throws IOException
	{
		reads++;
		
		if (bytesUntilNextMetadata==0)
		{
			readIcyMetadata();
			bytesUntilNextMetadata = metaInt;			
		}

		bytesUntilNextMetadata--;
		
		int readValue = is.read();

		return readValue;
	}
}					

