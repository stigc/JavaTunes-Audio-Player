package dk.stigc.javatunes.audioplayer.streams;

import java.net.*;
import java.util.*; 
import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.player.AudioInfoInternal;
import dk.stigc.javatunes.audioplayer.player.IAudio;

public class InputStreamHelper
{
	public int contentLength;
	public int icyMetaInt;
	private String contentType, icyName, icyGenre;
	
	private URLConnection createConnection(String location) throws Exception
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
	
	public InputStream getHttp(String location, String range) throws Exception
	{
		URLConnection conn = createConnection(location);
		conn.setRequestProperty("User-Agent", "JavaTunesPlayer"); 
		conn.setRequestProperty("Range", range);
		return getRemoteInputStreamImpl(conn);
	}
	
	public InputStream getHttpWithIcyMetadata(String url, AudioInfoInternal audioInfo) throws Exception
	{
		URLConnection conn = createConnection(url);
        //String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
        conn.setRequestProperty("User-Agent", "JavaTunesPlayer"); 
        conn.setRequestProperty("Icy-MetaData", "1");
        InputStream is = getRemoteInputStreamImpl(conn);

        //Set AudioInfo
		audioInfo.icyName = icyName;
		audioInfo.icyGenre = icyGenre;
        setCodecFromContentType(audioInfo);
        
        return is;
	}
	
	private void setCodecFromContentType(AudioInfoInternal audioInfo)
	{
        if (contentType != null)
        {
        	if (contentType.equals("video/mp4"))
        		audioInfo.codec = Codec.aac;
        	else if (contentType.equals("audio/aac"))
        		audioInfo.codec = Codec.aac;
        	else if (contentType.equals("audio/ogg"))
        		audioInfo.codec = Codec.ogg;
        	else if (contentType.equals("audio/ogg; codecs=vorbis"))
        		audioInfo.codec = Codec.vorbis;        	
        	else if (contentType.equals("audio/ogg; codecs=opus"))
        		audioInfo.codec = Codec.opus;
        	else if (contentType.equals("audio/ogg; codecs=flac"))
        		audioInfo.codec = Codec.flac;        	
        	else if (contentType.equals("audio/flac"))
        		audioInfo.codec = Codec.flac;
        	else if (contentType.equals("audio/mp3"))
        		audioInfo.codec = Codec.mp3;
        }
	}
	
	private InputStream getRemoteInputStreamImpl(URLConnection url) throws Exception
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
				
				if (httpStatus != 200)
					Log.write ("Not http 200 status -> " + httpStatus);
				
				icyMetaInt = httpConnection.getHeaderFieldInt("icy-metaint", 0);
				icyName = httpConnection.getHeaderField("icy-name");
				icyGenre = httpConnection.getHeaderField("icy-genre");
				contentLength = httpConnection.getContentLength();
				contentType = httpConnection.getContentType();
				return httpConnection.getInputStream();
    		}
    		catch(FileNotFoundException ex1)
    		{
    			throw ex1;
    		}
    		catch(Exception ex2)
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
}

