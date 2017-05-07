package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;
import dk.stigc.javatunes.audioplayer.streams.*;

public class InputstreamSelector
{
	public int granules;
	public long contentLength;
	public boolean isRemote;
	private int bufferSize = 6500; //Most Ogg files last page is within this size.
    
	public InputStream getInputStream(IAudio audio, AudioInfo audioInfo) throws Exception
	{
		isRemote = StringFunc.startsWithIgnoreCase(audio.getPath(), "http");
		
		if (!isRemote)
		{
			File file = new File(audio.getPath());
			if (file.exists() == false)
				throw new Exception(audio.getPath() + " does not exists");
			contentLength = file.length();
			return new FileInputStream(file);
		}
		
		//if (song.getCodec()==Codec.vorbis && song.getLengthInSeconds()==0)
		//	detectVorbisPlayLength(song);

		InputStreamHelper ish = new InputStreamHelper();
		InputStream is = ish.httpGetWithIcyMetadata(audio.getPath());
		contentLength = ish.contentLength;
		
    	if (audioInfo.Codec == Codec.unknown)
    	{
    		is = new InputStreamWithTypeParser(is);
    		InputStreamType type = ((InputStreamWithTypeParser)is).tryIdentifyStream();
    		
    		if (type == InputStreamType.AACADTS)
    			audioInfo.Codec = Codec.aacadts;
    		else if (type == InputStreamType.OGG)
    			audioInfo.Codec = Codec.vorbis;    		
    		else
    			audioInfo.Codec = Codec.mp3;	    	
    		
    		/*
    		if (type == InputStreamType.EXTM3U)
    		{
    			Common.close(is);
    			is = ish.httpGetWithIcyMetadata(song.file);
    			PlayListReader plr = new M3uReader();
    			plr.read("dummy", is);
    			
    			Log.write("Reading EXTM3U with song count " + plr.remotes.size());
    			if (plr.remotes.size() > 0)
    			{
	    			Song newSong = plr.remotes.get(0);
	    			song.copyFrom(newSong);
	    			return getInputStream(newSong);
    			}
    		}
    		*/
    	}
		
		if (ish.icyMetaInt > 0)
		{			
			//InputStreamWithTagReader is2 = new InputStreamWithTagReader(is, song);
			IcyMetadataInputStream icy = 
					new IcyMetadataInputStream(is, audioInfo, ish.icyMetaInt);
			is = icy;
		}
		
		if (ish.isStreamingRadio())
		{
			audioInfo.icyName = ish.icyName;
			audioInfo.icyGenre = ish.icyGenre;
		}
		
		return is;
	}
	
	/*
	void detectVorbisPlayLength(IAudio song)
	{
		if (song.isRemote())
			detectPlayLengthRemote(song);
		else
			detectPlayLengthFile(song);
	}*/
	
	void detectPlayLengthRemote(IAudio song)
	{
		try
		{
			InputStreamHelper ish = new InputStreamHelper();
			byte[] data = new byte[bufferSize];
			String range = "bytes=-" + bufferSize;
			InputStream is = ish.getRemoteInputStream(song.getPath(), range);
			
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
	
	void detectPlayLengthFile(IAudio song)
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

