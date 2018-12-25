package dk.stigc.javatunes.audioplayer.streams;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class CopyInputstream extends InputStream
{
	InputStream is;
	FileOutputStream fos;
	public CopyInputstream(InputStream is) throws FileNotFoundException
	{
		this.is = is;
		this.fos = new FileOutputStream("javatunes-raw-output-" + UUID.randomUUID().toString());
	}
	
	@Override
	public int read() throws IOException
	{
		int b = is.read();
		fos.write(b);
		return b;
	}
	
	@Override
	public void close() throws IOException
	{
		fos.close();
		super.close();
	}

}
