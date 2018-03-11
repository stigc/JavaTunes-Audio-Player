package dk.stigc.javatunes.audioplayer.player;

import java.io.*;
import java.nio.charset.Charset;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.streams.*;

public class InputSelector
{
	public long contentLength;
	public boolean isRemote;
	
	public int granules;
	private int bufferSize = 6500; //Most Ogg files last page is within this size.
    
	public InputStream getInputStream(IAudio audio, AudioInfo audioInfo) throws Exception
	{
		isRemote = StringFunc.startsWithIgnoreCase(audio.getPath(), "http");
		
		if (!isRemote)
		{
			//Todo: unknown codec?
			File file = new File(audio.getPath());
			if (file.exists() == false || file.isDirectory())
				throw new Exception(file.getAbsolutePath() + " does not exists");
			contentLength = file.length();
			
			if (audioInfo.lengthInSeconds == 0 && audio.getCodec() == Codec.vorbis)
				findOggGranules(audio);
			
			return new FileInputStream(file);
		}
		
		return getHttpInputStream(audio.getPath(), audio, audioInfo);
	}

	private InputStream getHttpInputStream(String url, IAudio audio, AudioInfo audioInfo) throws Exception, UnsupportedEncodingException, IOException
	{
		if (audioInfo.lengthInSeconds == 0 && audio.getCodec() == Codec.vorbis)
			findOggGranulesOnRemoteFile(audio);
		
		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.getHttpWithIcyMetadata(url, audioInfo);
		contentLength = ish.contentLength;
		
    	if (audioInfo.codec == Codec.unknown)
    	{
    		InputStreamWithTypeParser parser = new InputStreamWithTypeParser(is, audioInfo);
    		
    		if (parser.isPlayList)
    		{
    			Common.close(parser);
    			String url2 = parsePlayListFindFirstPath(parser);
    			if (url2 == null)
    				throw new Exception("Cannot read .m3u or .pls");
    			return getHttpInputStream(url2, audio, audioInfo);
    		}
    		
    		is = parser;
    	}
    	
    	if (audioInfo.codec == Codec.unknown)
    		audioInfo.codec = Codec.mp3;
		
		if (ish.icyMetaInt > 0)
			is = new IcyMetadataInputStream(is, audioInfo, ish.icyMetaInt);
		return is;
	}

	//Very simple .pls and .m3u parser.
	private String parsePlayListFindFirstPath(InputStreamWithTypeParser parser)
			throws UnsupportedEncodingException, IOException
	{
		String path = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(parser, "UTF-8"));
		for (int i=0; i<100 && path == null; i++)
		{
			String line = br.readLine();
			if (line == null)
				break;
			if (line.indexOf("File")==0 && line.indexOf("=")>0)
				path = line.substring(line.indexOf("=")+1); 
			else if (line.indexOf("http://") == 0)
				path = line;
			else if (line.indexOf("http2://") == 0)
				path = line;
		}
		return path;
	}
	
	void findOggGranulesOnRemoteFile(IAudio song)
	{
		try
		{
			InputStreamHelper ish = new InputStreamHelper();
			byte[] data = new byte[bufferSize];
			String range = "bytes=-" + bufferSize;
			InputStream is = ish.getHttp(song.getPath(), range);
			
			if (ish.contentLength >= 0)
			{
				bufferSize = InputStreamHelper.readToArray(is, data);
				findGranulesInBuffer(data);
			}

			Common.close(is);
		}
        catch(Exception ex)
        {
        	Log.write(ex);
        }  		
	}
	
	void findOggGranules(IAudio song)
	{
		try
		{
			File file = new File(song.getPath());
			long length = file.length();
			FileInputStream fis = new FileInputStream(file);
	    	BufferedInputStream bis = new BufferedInputStream(fis);
    		if (length>bufferSize)
    			bis.skip(length-bufferSize);
    		
    		byte[] data = new byte[bufferSize];
    		bufferSize = InputStreamHelper.readToArray(bis, data);
    		Common.close(bis);
    		
    		findGranulesInBuffer(data);
        } 
        catch(Exception ex)
        {
        	Log.write(ex);
        }       
	}

	void findGranulesInBuffer(byte[] data)
	{
		for (int i=bufferSize-4; i>=0; i--)
		if (data[i]==0x4F && data[i+1]==0x67 && data[i+2]==0x67 && data[i+3]==0x53) 
		{	
			granules = read32(data,i+6);
			Log.write("Ogg granules " + granules);
			break;
		}		
	}

	int touint(byte n) 
	{
		return (n & 0xff);
	}
	
	int read32(byte[] data, int ptr) 
	{
		int val = 0;
		val  = (touint(data[ptr]) & 0x000000ff);
		val |= (touint(data[ptr+1]) << 8  & 0x0000ff00);
		val |= (touint(data[ptr+2]) << 16 & 0x00ff0000);
		val |= (touint(data[ptr+3]) << 24 & 0xff000000);
		return val;
	}
}

