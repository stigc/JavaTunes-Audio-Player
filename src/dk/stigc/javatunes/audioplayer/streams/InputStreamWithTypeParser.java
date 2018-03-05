package dk.stigc.javatunes.audioplayer.streams;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import dk.stigc.javatunes.audioplayer.other.Codec;
import dk.stigc.javatunes.audioplayer.player.AudioInfo;

public class InputStreamWithTypeParser extends InputStream
{
	InputStream in;
	int bytesInBuffer, readFromBuffer;
	private byte[] buffer;
	public boolean isEXTM3U;
	
	private boolean bufferStartsWith(String tag)
	{
		Charset cs = Charset.forName("ISO-8859-1");
		byte[] tagAsBytes = tag.getBytes(cs);
		for (int i=0; i<tagAsBytes.length; i++)
			if (tagAsBytes[i] != buffer[i])
				return false;
		return true;
	}
	
	public InputStreamWithTypeParser(InputStream in, AudioInfo audioInfo)
	{
		this.in = in;
		
		buffer = new byte[1024*10];
		bytesInBuffer = InputStreamHelper.readToArray(in, buffer);
		
		if (bufferStartsWith("OggS"))
			audioInfo.codec = Codec.vorbis;
		else if (isAdts())
			audioInfo.codec = Codec.aacadts;
		else if (bufferStartsWith("#EXTM3U"))
			isEXTM3U = true;
	}

	private int getBufferIndex(int index)
	{
		return buffer[index] & 0xff;
	}
	
	private byte getBit(byte value, int index)
	{
		if ((value & (1<<index)) == 0)
			return 0;
		return 1;
	}

	private byte getBit(int value, int index)
	{
		if ((value & (1<<index)) == 0)
			return 0;
		return 1;
	}
	
	private int isHeader(int i)
	{
		//FIND FFF
		if (getBufferIndex(i)==0xFF && ((getBufferIndex(i+1)>>4)&0xF)==0xF)
		{
			int b1 = buffer[i+1];
			byte b3 = buffer[i+3];
			byte b4 = buffer[i+4];
			byte b5 = buffer[i+5];
			
			/*
			Log.write("bits " + getBit(b1, 7) 
					+ "" + getBit(b1, 6) 
					+ "" + getBit(b1, 5)
					+ "" + getBit(b1, 4)
					+ "" + getBit(b1, 3)
					+ "" + getBit(b1, 2)
					+ "" + getBit(b1, 1)
					+ "" + getBit(b1, 0));
			*/
			
			//http://opencore-aacdec.googlecode.com/svn/trunk/src/get_adts_header.c
			//Layer == '00' for AAC 
			if (getBit(b1, 1)!=0 || getBit(b1, 2)!=0)
				return -1;

			int frameLength = 0;
			frameLength = frameLength | getBit(b3, 2) << 12;
			frameLength = frameLength | getBit(b3, 1) << 11;
			
			frameLength = frameLength | getBit(b4, 7) << 10;
			frameLength = frameLength | getBit(b4, 6) << 9;
			frameLength = frameLength | getBit(b4, 5) << 8;
			frameLength = frameLength | getBit(b4, 4) << 7;
			frameLength = frameLength | getBit(b4, 3) << 6;
			frameLength = frameLength | getBit(b4, 2) << 5;
			frameLength = frameLength | getBit(b4, 1) << 4;
			frameLength = frameLength | getBit(b4, 0) << 3;

			frameLength = frameLength | getBit(b5, 7) << 2;
			frameLength = frameLength | getBit(b5, 6) << 1;
			frameLength = frameLength | getBit(b5, 5) << 0;
			return frameLength;
		}
		
		return -1;
	}
	
	private boolean isAdts()
	{
		for (int i=0; i<bytesInBuffer-8; i++)
		{
			int frameLength = isHeader(i);
			
			//Log.write("frameLength is " + frameLength);
			if (frameLength > 0 && frameLength + i + 1 < buffer.length
					&& getBufferIndex(i) == getBufferIndex(i + frameLength)
					&& getBufferIndex(i+1) == getBufferIndex(i + frameLength+1))			
			return true;
		}
		return false;
	}
			

	public int read() throws IOException 
	{
		if (readFromBuffer<bytesInBuffer)
		{
			readFromBuffer++;
			return getBufferIndex(readFromBuffer-1);
		}
		
		return in.read();
	}
}
