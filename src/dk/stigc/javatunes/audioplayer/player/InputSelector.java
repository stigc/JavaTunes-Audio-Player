package dk.stigc.javatunes.audioplayer.player;

import java.io.*;
import java.nio.charset.Charset;

import dk.stigc.common.StringFunc3;
import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.streams.*;
import dk.stigc.javatunes.audioplayer.tagreader.TagBase;
import dk.stigc.javatunes.audioplayer.tagreader.TagOgg;
import dk.stigc.javatunes.audioplayer.tagreader.TagReaderManager;

public class InputSelector
{
	public long contentLength;
	public boolean isRemote;
	
	public int granules;
	private int bufferSize = 32*1024; //Most Ogg files last page is within this size.
    
	public InputStream getInputStream(IAudio audio, AudioInfoInternal audioInfo) throws Exception
	{
		isRemote = StringFunc3.startsWithIgnoreCase(audio.getPath(), "http");
		
		if (isRemote)
			return getRemoteStream(audio.getPath(), audioInfo);
		
		File file = new File(audio.getPath());
		if (file.exists() == false || file.isDirectory())
			throw new Exception(file.getAbsolutePath() + " does not exists");
		
		contentLength = file.length();
		
		if (isOggContainer(audioInfo))
			findOggGranules(file);
		
		if (audioInfo.isCodec(Codec.mp4container))
		{
			Track track = new TagReaderManager().read(file);
    		if (track != null)
    			audioInfo.setCodec(track.codec);
		}
			
		return new FileInputStream(file);
	}

	private boolean isOggContainer (AudioInfoInternal audioInfo)
	{
		return audioInfo.getCodec() == Codec.vorbis 
				|| audioInfo.getCodec() == Codec.opus
				|| audioInfo.getCodec() == Codec.oggcontainer;
	}
	
	private InputStream getRemoteStream(String url, AudioInfoInternal audioInfo) throws Exception, UnsupportedEncodingException, IOException
	{
		if (url == null)
			url = audioInfo.getNewLocation();
		
		InputStreamHelper ish = new InputStreamHelper();
		InputStreamImpl is = ish.getHttpWithIcyMetadata(url, audioInfo);
		contentLength = ish.contentLength;
		
		if (audioInfo.isCodec(Codec.hlc))
		{
			contentLength = 0;
			HlsInputStream hlsStream = new HlsInputStream();
			HlsSegmentsReader reader = new HlsSegmentsReader(url, is, hlsStream);
			reader.start();
			return hlsStream;
		}
		else if (audioInfo.isCodec(Codec.unknown, Codec.oggcontainer))
    	{
			CodecParser parser = new CodecParser(is.getBuffer());
			
    		if (parser.isPlayList)
    		{
    			String url2 = parsePlayListFindFirstPath(is);
    			if (url2 == null)
    				throw new Exception("Cannot read .m3u or .pls");
    			audioInfo.setNewLocation(url2);
    			return getRemoteStream(null, audioInfo);
    		} 
    		else if (parser.codec != null)
    		{
    			audioInfo.setCodec(parser.codec);
    		}
    	}
    	
    	if (audioInfo.isCodec(Codec.unknown))
    		audioInfo.setCodec(Codec.mp3);
    	
    	//playing file via http
    	if (contentLength != -1 && isOggContainer(audioInfo))
			findOggGranulesOnRemoteFile(url);
		
		if (ish.icyMetaInt > 0)
			return new IcyMetadataInputStream(is, audioInfo, ish.icyMetaInt);
		
		return is;
	}

	//Very simple .pls and .m3u parser.
	private String parsePlayListFindFirstPath(InputStream is)
			throws UnsupportedEncodingException, IOException
	{
		String path = null;
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
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
	
	void findOggGranulesOnRemoteFile(String url)
	{
		try
		{
			InputStreamHelper ish = new InputStreamHelper();
			byte[] data = new byte[bufferSize];
			String range = "bytes=-" + bufferSize;
			InputStream is = ish.getHttp(url, range);
			
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
	
	void findOggGranules(File file)
	{
		try
		{
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
		//find OggS
		for (int i=bufferSize-4; i>=0; i--)
		if (data[i]==0x4F && data[i+1]==0x67 
		&& data[i+2]==0x67 && data[i+3]==0x53) 
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

