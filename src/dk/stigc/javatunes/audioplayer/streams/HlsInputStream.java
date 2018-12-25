package dk.stigc.javatunes.audioplayer.streams;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HlsInputStream extends InputStream
{
	int counter;
	int currentIndex;
	byte[] currentBytes;
	public LinkedBlockingQueue<byte[]> data = new LinkedBlockingQueue<byte[]>(3);

	@Override
	public int read() throws IOException
	{
		try
		{
			if (currentBytes == null)
			{
				counter++;
				currentBytes = data.poll(10, TimeUnit.SECONDS);
				if (currentBytes == null)
					throw new IOException("Nothing from stream i 10 seconds");
				if (currentBytes.length == 0)
					return -1;
			}
			
			if (currentBytes.length == currentIndex)
			{
				currentIndex = 0;
				currentBytes = null;
				return read();
			}
			byte b = currentBytes[currentIndex++];
			return 0x00 << 24 | b & 0xff;
		} 
		catch (InterruptedException e)
		{
			return -1;
		}
	}
}
