package dk.stigc.javatunes.audioplayer.streams;

import java.net.*;
import java.util.*; 
import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.AudioInfoInternal;
import dk.stigc.javatunes.audioplayer.player.IAudio;

public class InputStreamHelper
{
	public boolean logHeaders;
	public int contentLength;
	public int icyMetaInt;
	private String contentType, icyName, icyGenre;
	private URLConnection createConnection(String location) throws IOException
	{
		location = location.replace('\\','/');
		location = location.replaceAll(" ","%20");
				
		Log.write("loading: " + location);
		URL url = new URL(location);
		URLConnection urlc = url.openConnection();	
		urlc.setConnectTimeout(5 * 1000);
		urlc.setReadTimeout(10000);
	    return urlc;	
	}
	
	public InputStream getHttp(String location, String range) throws IOException
	{
		URLConnection conn = createConnection(location);
		conn.setRequestProperty("User-Agent", "JavaTunesPlayer"); 
		if (range != null)
			conn.setRequestProperty("Range", range);
		return getRemoteInputStreamImpl(conn);
	}
	
	
	public InputStreamImpl getHttpWithIcyMetadata(String url, AudioInfoInternal audioInfo) throws Exception
	{
		URLConnection conn = createConnection(url);
        conn.setRequestProperty("User-Agent", "JavaTunesPlayer"); 
        conn.setRequestProperty("Icy-MetaData", "1");
        InputStream is = getRemoteInputStreamImpl(conn);

        if (icyMetaInt>0)
		{
			audioInfo.setIcyData(icyMetaInt, icyName, icyGenre);
		}
			
        setCodecFromContentType(audioInfo);
        
        return new InputStreamImpl(is);
	}
	
	private void setCodecFromContentType(AudioInfoInternal audioInfo)
	{
        if (contentType != null)
        {
        	if (contentType.equals("video/mp4"))
        		audioInfo.setCodec(Codec.aac);
        	else if (contentType.equals("audio/aac"))
        		audioInfo.setCodec(Codec.aac);
        	else if (contentType.equals("audio/ogg"))
        		audioInfo.setCodec(Codec.oggcontainer);
        	else if (contentType.equals("audio/ogg; codecs=vorbis"))
        		audioInfo.setCodec(Codec.vorbis);        	
        	else if (contentType.equals("audio/ogg; codecs=opus"))
        		audioInfo.setCodec(Codec.opus);
        	else if (contentType.equals("audio/ogg; codecs=flac"))
        		audioInfo.setCodec(Codec.flac);        	
        	else if (contentType.equals("audio/flac"))
        		audioInfo.setCodec(Codec.flac);
        	else if (contentType.equals("audio/mp3"))
        		audioInfo.setCodec(Codec.mp3);
        	else if (contentType.equals("audio/mp3"))
        		audioInfo.setCodec(Codec.mp3);
        	else if (contentType.contains("vnd.apple.mpegurl"))
        		audioInfo.setCodec(Codec.hlc);            	
        }
	}
	
	private InputStream getRemoteInputStreamImpl(URLConnection url) throws IOException
	{
		HttpURLConnection httpConnection = (HttpURLConnection)url;
		httpConnection.setInstanceFollowRedirects(true);
		int maxTries = 3;
		
		while (true)
		{		
    		try
    		{
    			maxTries--;

    			httpConnection.connect();
    			
				int httpStatus = httpConnection.getResponseCode();
				
				if (httpStatus == 404)
					throw new FileNotFoundException("404");
				
				//if (httpStatus != 200)
				//	Log.write ("Not http 200 status -> " + httpStatus);
				
				icyMetaInt = httpConnection.getHeaderFieldInt("icy-metaint", 0);
				icyName = httpConnection.getHeaderField("icy-name");
				icyGenre = httpConnection.getHeaderField("icy-genre");
				contentLength = httpConnection.getContentLength();
				contentType = httpConnection.getContentType();
				
				if (logHeaders)
				{
					for (Map.Entry<String, List<String>> entry : httpConnection.getHeaderFields().entrySet())
					{
						String str = "";
			            for (String value : entry.getValue()) {
			            	str += (value + ", ");
			            }
			            
			            Log.write("Header " + entry.getKey() + ": " + str);
			        }
				}
				return httpConnection.getInputStream();
    		}
    		catch(FileNotFoundException ex1)
    		{
    			throw ex1;
    		}
    		catch(IOException ex2)
    		{
    			if (maxTries > 0)
    			{
    				Log.write ("Try Again");
    				Common.sleep(200);
    			}
    			else
    			{
    				throw ex2;
    			}
    		}
		}
	}	

	//Fills a array from a BufferedInputStream
	public static int readToArray(InputStream bis, byte[] data)
	{
		int index = 0;
		int read = 0;
		int size = data.length;
		try
		{ 			
			do
			{
				read = bis.read(data, index, size-index);
				//Log.write("read " + read);
				if (read>0) index += read;
			}
			while 
			(index<size && read!=-1);
		}
		catch(Exception ex)
        {
        	Log.write("Ex: " + ex);
        }
        
		return index;
        //if (index<size)
        //	Log.write("[ERROR reading full buffer : " + index + "]"); 
	}
	
	public static byte[] readBytes(InputStream in, int max) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream(max);
		byte[] buf = new byte[2048];
		int n=0;		
		while ((n = in.read(buf)) > 0)
		{
			baos.write(buf, 0, n);
			if (baos.size() > max)
				break;
		}
		return baos.toByteArray();
	}
	
	public static byte[] readBytes(InputStream in)
	{
		byte[] data = null;
		
		try
		{ 
			ByteArrayOutputStream out = new ByteArrayOutputStream(32000);
			byte[] buf = new byte[8000];
			int n=0;		
			while ((n = in.read(buf)) > 0)
				out.write(buf, 0, n);
			data = out.toByteArray();
		}
		catch(Exception ex)
        {
        	Log.write("readBytes(2)", ex);
        }
        finally
        {
        	Common.close(in);
        }	
        
        return data;	
	}
}

