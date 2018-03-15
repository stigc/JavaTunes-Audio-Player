package dk.stigc.javatunes.audioplayer.streams;

import java.io.*;
import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.AudioInfoInternal;
import dk.stigc.javatunes.audioplayer.tagreader.*;

//http://jicyshout.sourceforge.net/oreilly-article/java-streaming-mp3-pt2/java-streaming-mp3-pt2.html
//http://sphere.sourceforge.net/flik/docs/streaming.html
//http://www.smackfu.com/stuff/programming/shoutcast.html
public class InputStreamWithTagReader extends BufferedInputStream
{
	private AudioInfoInternal audioInfo;
	private int buffersize = 1024*8; //8kb;
	private byte[] data = new byte[buffersize];
	private int metaInt = -1;
	private int bytesUntilNextMetadata = -1;
	private boolean headerRead;
	private FileOutputStream os;
	private int reads;
	
	public void setMetaIntFromHeader(int metaInt)
	{
		if (metaInt>0)
		{
			Log.write("IcyMetaData in http header : " + metaInt);
			this.metaInt = metaInt;
			this.bytesUntilNextMetadata = metaInt;
			headerRead = true;
		}
	}
	
	public InputStreamWithTagReader(InputStream in, AudioInfoInternal audioInfo)
	{
		super(in);
		this.audioInfo = audioInfo;
	}
	
	private boolean isHeader(byte[]b, String tag)
	{
		for (int i=0; i<tag.length(); i++)
		{	
			char c1 = Character.toLowerCase(tag.charAt(i));
			char c2 = Character.toUpperCase(c1);
			if (b[i]!=c1 && b[i]!=c2) return false;
		}
		
		return true;
	}
	
	public int read(byte[] b)  throws IOException
	{
		return read(b, 0, b.length);
	}
	
	private String parseIcyStreamTitle(String v)
	{
		v = v.trim();
		if (v.toLowerCase().startsWith("streamtitle='"))
		{
			v = v.substring(13);
			int end = v.indexOf("';");
			if (end>-1)
				v = v.substring(0, end);
		}
		
		return v;
	}
	
	private void readIcyMetadata() throws IOException
	{
		int blocks = super.read();
		//Log.write("readIcyMetadata: " + blocks);
					
		if (blocks>0)
		{
			int bytes = blocks*16;
			byte[] data = new byte[bytes];
			int bytesRead = super.read(data, 0, bytes);	
			String metaData = new String(data);
			//Log.write("metaData: " + metaData);			
			String nowPlaying = parseIcyStreamTitle(metaData);
			
			if (StringFunc.isNullOrEmpty(nowPlaying))
				return;
			
			synchronized (audioInfo)
			{
				audioInfo.icyStreamTitle = nowPlaying;
			}
		}
	}	

	public int read(byte[] b, int off, int len)  throws IOException
	{
		reads++;

		//Log.write("Try read: " + len);
		//Ved ICY streams skal vi kunne komme tilbage til index 0;
		//Kun ved første read.
		if (!headerRead)
			mark(len);
		
		if (bytesUntilNextMetadata>0)
		{
			//Log.write("bytesUntilNextMetadata: " + bytesUntilNextMetadata);
			len = Math.min(len, bytesUntilNextMetadata);
		}
		else if (bytesUntilNextMetadata==0)
		{
			readIcyMetadata();
			bytesUntilNextMetadata = metaInt;
			return 0;
		}
		
		//Do the reading...
		int read = super.read(b, off, len);

		int minRead = Math.min(buffersize, len);
		
		while (read < minRead)
			read += super.read(b, off+read, minRead-read);
				
		//Log.write("read " + read);
		
		if (os!=null && read>0)
			os.write(b,off,read);
		
		
		if (bytesUntilNextMetadata>0)
			bytesUntilNextMetadata -= read;

		if (!headerRead)
		{
			headerRead = true;
			boolean id3 = isHeader(b, "ID3");
			boolean ogg = isHeader(b, "OggS");

			Log.write ("id3: " + id3);
			Log.write ("ogg: " + ogg);


			if (id3 || ogg)
			{
				int length = read;
				if (length>buffersize) length = buffersize-1;
				System.arraycopy(b, 0, data, 0, length);				

				//if (id3) Log.write("ID3");
				//if (ogg) Log.write("ogg");
				
				TagBase tr = null;
				if (id3) tr = new TagId3V2();
				if (ogg) tr = new TagOgg();
				if (tr.parse(null, data, false))
				{
					//TODO fixe with callback, whatever...
					Log.write("Tag read from stream " + tr);
				}	
			}	
		}

		//Log.write("Read " + read);
		return read;	
	}
}					

