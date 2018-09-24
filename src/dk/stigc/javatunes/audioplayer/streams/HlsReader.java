package dk.stigc.javatunes.audioplayer.streams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import dk.stigc.javatunes.audioplayer.other.Log;

public class HlsReader extends Thread
{
	int currentIndex = 0;
	byte[] currentBytes;
	public LinkedBlockingQueue<String> segments = new LinkedBlockingQueue<String>();
	HlsInputStream hlsStream;
	InputStream is;
	private String url;
	private String baseUrl;
	
	public HlsReader(String url, InputStream is, HlsInputStream hlsStream) throws UnsupportedEncodingException, IOException
	{
		this.url = url;
		this.is = is;
		this.hlsStream = hlsStream;
		
		if (url.endsWith(".m3u8"))
			baseUrl = url.substring(0, url.lastIndexOf("/"));
		
		parseSegments(is);
	}
	
	private void parseSegments(InputStream is) throws UnsupportedEncodingException, IOException
	{
		String last = segments.peek();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		boolean next = false;
		while((line = br.readLine()) != null)
		{
			if (next)
			{
				String url = line.startsWith("http") ? line : baseUrl + "/" + line;
				if (last == null || last.compareTo(url) < 0)
					segments.add(url);
			}
			next = line.startsWith("#EXTINF");
		}
		Log.write("Found " + segments.size() + " segments");
	}
	
	private void readNextSegments() throws IOException
	{
		Log.write("Read more segments");
		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.getHttp(url, null);
		parseSegments(is);
	}
	
	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				String url = segments.poll(10, TimeUnit.SECONDS);
				
				if (url == null)
					break;
				
				if (segments.size() <= 3)
					readNextSegments();

				readToEnd(url);
			}
		} 
		catch (Exception e)
		{
			Log.write(e);
		}
	}
	
	public void readToEnd(String url) throws IOException, InterruptedException
	{
		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.getHttp(url, null);
		
		if (ish.contentLength>0)
			is = new ContentLengthAwareInputStream(is, ish.contentLength);
		
		byte[] bytes = InputStreamHelper.readBytes(is);
		//Log.write(bytes.length + "/" + bytes[188]);
		hlsStream.data.put(bytes);
	}
}
