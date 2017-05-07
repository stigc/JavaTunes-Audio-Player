package dk.stigc.javatunes.audioplayer.tagreader;

import java.io.*;

public class FileBuffer 
{
	int readBytes, length, counter;
	final int max = 1024*1024; //Max 1MB
	public byte buffer[] = null;
	String fn;
    FileInputStream in;

	public void setFile(File file) throws FileNotFoundException
	{
		if (buffer==null)
			buffer = new byte[max];
			
		readBytes = 0;
		fn = file.getName();
		length = (int)file.length();
		in = new FileInputStream(file);
		//load init 5kb
		loadBuffer(1024*5);
	}

	public String getFileName()
	{
		return fn;
	}
	
	public void close()
	{
		try
		{	
			in.close();
		}
		catch(Exception e) {}
	}
	
	public int loadEnd(int bytes)
	{
		try
		{
			//Exciting Max?	
			if (bytes>max) 
				bytes=max;
							
			in.getChannel().position(0);
			int skip = length-bytes;
			in.skip(skip);
        	in.read(buffer, 0, bytes);		
			readBytes = bytes;
		}
		catch(Exception e)
		{
		}
		
		return readBytes;
	}	
	
	public void loadEnd()
	{
		try
		{
			//Mp3 id3v1 - Læs de sidste 128 bytes...
			int skip = length-128;
			in.getChannel().position(0);
			in.skip(skip);
        	in.read(buffer, 0, 128);		
			readBytes = 128;
		}
		catch(Exception e) {}
	}
		
	public int loadBuffer(int v)
	{	
		//Less than already read.
		if (v<=readBytes) 
			return readBytes;
		
		//Crossing Max?	
		if (v>max) 
			v=max;
		
		try
		{	
			//counter++;
			//Log.write("Reading from " + readBytes + " to " + v);
			readBytes += in.read(buffer, readBytes, v-readBytes);
		}
		catch(Exception e){}
		return readBytes;
	}
}