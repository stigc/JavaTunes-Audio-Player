package dk.stigc.javatunes.audioplayer.streams;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import dk.stigc.javatunes.audioplayer.other.Common;
import dk.stigc.javatunes.audioplayer.other.Log;

public class HlsSegmentsReader extends Thread
{
	private int maxSegments = 512;
	private Queue<String> segments = new LinkedList<String>();
	private HlsInputStream hlsStream;
	private String url;
	private String baseUrl;
	
	public HlsSegmentsReader(String url, InputStream is, HlsInputStream hlsStream) 
			throws UnsupportedEncodingException, IOException
	{
		this.url = url;
		this.hlsStream = hlsStream;
		
		if (url.endsWith(".m3u8"))
			baseUrl = url.substring(0, url.lastIndexOf("/"));
		
		parseSegments(is, null);
		
		if (segments.size()==0)
			throw new RuntimeException("No HLS segments in " + url);
	}
	
	private void parseSegments(InputStream is, String last) throws UnsupportedEncodingException, IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		String line = null;
		boolean nextIsSegmentUrl = false;
		while((line = br.readLine()) != null)
		{
			if (nextIsSegmentUrl)
			{
				String url = line.startsWith("http") ? line : baseUrl + "/" + line;
				if (last == null || last.compareTo(url) < 0)
					segments.add(url);
			}
			
			nextIsSegmentUrl = line.startsWith("#EXTINF");
			
			//evil protection ...
			if (segments.size() > maxSegments)
				return;
		}
		
		Log.write("Found " + segments.size() + " segments");
	}
	
	private void readNextSegments(String lastSegment) throws IOException
	{
		Log.write("Read more segments");
		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.getHttp(url, null);
		parseSegments(is, lastSegment);
		Common.close(is);
	}
	
	@Override
	public void run()
	{
		try
		{
			while (true)
			{
				//end of stream
				if (segments.size() == 0)
				{
					//NULL = EOS mark
					hlsStream.data.put(new byte[0]);
					return;
				}
				
				String url = segments.poll();
				
				if (segments.size() == 0)
					readNextSegments(url);
				
				readBytesInUrlAndAddToHlsStream(url);
			}
		} 
		catch (Exception e)
		{
			Log.write(e);
		}
	}
	
	public void readBytesInUrlAndAddToHlsStream(String url) throws IOException, InterruptedException
	{
		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.getHttp(url, null);
		
		if (ish.contentLength>0)
			is = new ContentLengthAwareInputStream(is, ish.contentLength);
		
		byte[] bytes = InputStreamHelper.readBytes(is);
		
		if (bytes.length>0)
		{
			hlsStream.data.put(bytes);
		}
	}
}
