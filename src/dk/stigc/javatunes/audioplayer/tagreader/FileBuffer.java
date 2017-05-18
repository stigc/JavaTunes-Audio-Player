package dk.stigc.javatunes.audioplayer.tagreader;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.Common;

public class FileBuffer 
{
	int readBytes, fileLength;
	final int max = 1024*1024*2; //Max 2MB
	public byte buffer[] = new byte[max];
	String fn;
    FileInputStream in;
    BufferedInputStream bis;
    
	public void setFile(File file) throws FileNotFoundException
	{
		readBytes = 0;
		fn = file.getName();
		fileLength = (int)file.length();
		in = new FileInputStream(file);
		bis = new BufferedInputStream(in);
		ensureBufferLoad(1024*4);
	}

	public String getFileName()
	{
		return fn;
	}
	
	public void close()
	{
		Common.close(bis);
	}
	
	public int loadEnd(int bytes)
	{
		try
		{
			//crossing Max?	
			if (bytes>max) 
				bytes=max;
							
			in.getChannel().position(0);
			int skip = fileLength-bytes;
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
			//Mp3 id3v1 - last 128 bytes...
			int skip = fileLength-128;
			in.getChannel().position(0);
			in.skip(skip);
        	in.read(buffer, 0, 128);		
			readBytes = 128;
		}
		catch(Exception e) {}
	}
		
	public int ensureBufferLoad(int bytes)
	{	
		//Less than already loaded
		if (bytes <= readBytes) 
			return readBytes;

		//more than file length?	
		if (bytes > fileLength) 
			bytes = fileLength;
		
		//more than max buffer size?	
		if (bytes > max) 
			bytes = max;
		 
		try
		{	
			while (readBytes < bytes)
				readBytes += bis.read(buffer, readBytes, bytes-readBytes);
		}
		catch(Exception e)
		{
			
		}
		return readBytes;
	}
}