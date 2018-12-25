package dk.stigc.javatunes.audioplayer.streams;

import java.io.IOException;
import java.io.InputStream;

import dk.stigc.javatunes.audioplayer.other.Log;

public class TsInputStream extends InputStream
{
	InputStream source;
	byte[] currentTsPacket;
	int currentIndex;
	private boolean eos;
	
	public TsInputStream(InputStream source)
	{
		super();
		this.source = source;
	}

	int totalIndex;
	private byte readInternal() throws IOException
	{
		//totalIndex++;
		//Log.write("totalIndex:" + totalIndex);
		
		byte b = (byte)source.read();
		return b;	
	}
	
	@Override
	public int read() throws IOException
	{
		while (currentTsPacket == null || currentIndex == currentTsPacket.length)
		{			
			currentTsPacket = readNextPacket();
			if (eos)
				return -1;
			
			currentIndex = 0;
		}
		
		byte b = currentTsPacket[currentIndex];
		currentIndex++;
		return 0x00 << 24 | b & 0xff;
	}

	//https://stackoverflow.com/questions/32386917/how-does-the-demux-differenctiate-between-the-0x47-that-is-the-sync-byte-and-0x4
	private byte[] readNextPacket() throws IOException
	{
		byte g = readInternal();

		if (g == -1) //EOS
		{
			eos = true;
			return null;
		}
		
		if (g != 71)
			throw new IOException("Stream is not in synch (expected G)");
		
		byte flags = readInternal();
		byte flags2 = readInternal();
		byte flags3 = readInternal();
		
		int tei = flags & 0x0001;
		int psi = (flags >> 1) & 0x0001;
		int tp = (flags >> 2) & 0x0001;
		int pid = ((flags << 8) | (flags2 & 0xff)) & 0x1fff;
		int scramblingControl = (flags3 >> 6) & 0x03;
		boolean hasAdaptationField = (flags3 & 0x20) != 0;
		boolean hasPayloadData  = (flags3 & 0x10) != 0;
		int continuityCount = flags3 & 0x0f;
		
		int bytesLeft = 188-4;
		
		if (hasAdaptationField)
		{
			int length = readInternal() & 0xff;
			//Log.write("length:_" + length + "; " + hasPayloadData + "; " + continuityCount);
			bytesLeft--;
			
			if (bytesLeft - length < 0)
				throw new RuntimeException("Overrun " + bytesLeft);

			for (int i=0; i<length; i++)
				readInternal();
			bytesLeft -= length;
		}
		
		if (bytesLeft <= 0)
			return null;
		
		byte[] tsPacker = new byte[bytesLeft];
		
		for (int i=0; i<bytesLeft; i++)
			tsPacker[i] = readInternal();
		
		if (pid != 0x0100)
			return null;
		//Log.write("AUDIO found");
		return tsPacker;
	}
	
}