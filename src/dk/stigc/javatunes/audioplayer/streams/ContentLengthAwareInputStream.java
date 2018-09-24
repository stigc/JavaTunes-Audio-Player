package dk.stigc.javatunes.audioplayer.streams;

import java.io.IOException;
import java.io.InputStream;

import dk.stigc.javatunes.audioplayer.other.*;

public class ContentLengthAwareInputStream extends InputStream
{
	int read;
	int contentLength;
	InputStream is;
	
	public ContentLengthAwareInputStream(InputStream is, int contentLength)
	{
		this.is = is;	
		this.contentLength = contentLength;
	}
	
	@Override
	public int read() throws IOException 
	{
		read ++;

		if (read > contentLength && contentLength > 0)
		{
			//Log.write("ContentLengthAwareInputStream End > " +contentLength);
			return -1;
		}
		
		int r = is.read();
		return r;
	}
}
