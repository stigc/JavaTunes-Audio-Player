package dk.stigc.javatunes.audioplayer.tagreader;

import dk.stigc.javatunes.audioplayer.other.*;

abstract public class TagBase extends AbstractTrack
{ 
	public byte[] imgData;
	protected boolean decodeImage;
	public boolean tagFound;
	abstract void parseImp(FileBuffer fb, byte[] external) throws Exception;

	public boolean parse(FileBuffer fb, boolean decodeImage)
	{
		return parse(fb, null, decodeImage);
	}
	   	
	public boolean parse(FileBuffer fb, byte[] external, boolean decodeImage)
	{
		this.decodeImage = decodeImage;
		tagFound = false;
		super.clear();
		
		try
		{
			parseImp(fb, external);
			if (tagFound)
				cleanUpData();
		}
		catch (Exception ex)
		{
			Log.write(ex);
		}
		
		return tagFound;
	}

	public void addYear(String v)
	{
		int tmp = parseNumberFromString(v);
		
		if (tmp >= 1000 || tmp <= 9999)
			year = tmp;
	}

	public void addDiscNumber(String v)
	{
		discNumber = parseNumberFromString(v);
	}
	
	public void addTrackNumber(String v)
	{
		trackNumber = parseNumberFromString(v);
	}
		
	public void addGenre(String v)
	{
		v = v.trim();
		if (v.length()>0)
			super.genres.add(v);
	}
		
	public void addArtist(String v)
	{
		v = v.trim();
		if (v.length()>0)
			super.artists.add(v);
	}
		
	public void cleanUpData()
	{
		if (title != null)
			title = title.trim();
		if (album != null)
			album = album.trim();
		if (lyrics != null)
			lyrics = lyrics.trim(); 
	}
	
	private static int parseNumberFromString(String str) 
	{
		String onlyDigits = "";
		for (int i=0; i<str.length(); i++)
		{
			char c = str.charAt(i);
			if (Character.isDigit(c)) 
			{
				//no leading zeroes
				if (c == '0' && onlyDigits.length() == 0)
					continue;
				onlyDigits+=c;
			}
			//stop at first non digit
			else
			{
				if (onlyDigits.length()>0)
					break;
			}
		}
		
		if (onlyDigits.length()==0)
			return 0;
		return Integer.parseInt(onlyDigits);
	}

	protected double getRG(String v, int index) 
	{
		double replaygain = AbstractTrack.REPLAY_GAIN_NOT_SET;
		try
		{
			String rgAsString = "";
			//v = v.substring(21, v.length()-3);
			for (int i=index; i<v.length(); i++)
			{
				if (Character.isDigit(v.charAt(i))
					|| v.charAt(i)=='-'
					|| v.charAt(i)=='.')
					rgAsString += v.charAt(i);
			}
			replaygain = Double.parseDouble(rgAsString);
		}
		catch (Exception ex)
		{
			
		}
		return replaygain;
	}	

    public static int synchSafeBytesToInt(byte[] buffer, int start) 
    {
    	return (buffer[start] << 21) + (buffer[start+1] << 14) + (buffer[start+2] << 7) + buffer[start+3];
	}

    final public static int read24Reverse(byte[] buffer, int start) 
    {
		int val = 0;
		val  =  ((buffer[start+2] & 0xff)        & 0x000000ff);
		val |= (((buffer[start+1] & 0xff) << 8)  & 0x0000ff00);
		val |= (((buffer[start+0] & 0xff) << 16) & 0x00ff0000);
		return val;
	}
	
	final public static int read32Reverse(byte[] buffer, int start) 
	{
		int val = 0;
		val  =  ((buffer[start+3] & 0xff)        & 0x000000ff);
		val |= (((buffer[start+2] & 0xff) << 8)  & 0x0000ff00);
		val |= (((buffer[start+1] & 0xff) << 16) & 0x00ff0000);
		val |= (((buffer[start+0] & 0xff) << 24) & 0xff000000);
		return val;
	}

	final public static int read16Reverse(byte[] buffer, int start) 
	{
		int val = 0;
		val  =  ((buffer[start+1] & 0xff)        & 0x000000ff);
		val |= (((buffer[start+0] & 0xff) << 8)  & 0x0000ff00);
		return val;
	}
		
	//Ogg, Ape
	final public static int read32(byte[] buffer, int start) 
	{
		int val = 0;
		val  =  ((buffer[start+0] & 0xff)        & 0x000000ff);
		val |= (((buffer[start+1] & 0xff) << 8)  & 0x0000ff00);
		val |= (((buffer[start+2] & 0xff) << 16) & 0x00ff0000);
		val |= (((buffer[start+3] & 0xff) << 24) & 0xff000000);
		return val;
	}			
}