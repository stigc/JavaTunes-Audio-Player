package dk.stigc.javatunes.audioplayer.streams;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.Log;

public class InputStreamImpl extends InputStream
{
	private InputStream in;
	private int readFromBuffer;
	private byte[] buffer = null;

	public InputStreamImpl(InputStream in)
	{
		this.in = in;
	}
	
	public byte[] getBuffer() throws IOException
	{
		if (buffer == null)
			buffer = InputStreamHelper.readBytes(in, 1024*16);
		Log.write("Buffered " + buffer.length);
		return buffer;
	}
	
	private int getBufferIndex(int index)
	{
		return buffer[index] & 0xff;
	}
	
	public int read() throws IOException 
	{
		if (buffer != null && readFromBuffer < buffer.length)
		{
			readFromBuffer++;
			return getBufferIndex(readFromBuffer-1);
		}
		
		return in.read();
	}
}