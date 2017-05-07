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
		v = cleanYear(v);
		if (v.length()>0)
			year = Integer.parseInt(v);
	}

	public void addDiscNumber(String v)
	{
		v = getTrackNumber(v);
		if (v.length()>0)
			discNumber = Integer.parseInt(v);
	}
	
	public void addTrackNumber(String v)
	{
		v = getTrackNumber(v);
		if (v.length()>0)
			trackNumber = Integer.parseInt(v);
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
	
	private static String cleanYear(String v) 
	{
		String r = "";
		if (v.length()<4) return "";
		
		for (int i=0; i<4; i++)
			if (Character.isDigit(v.charAt(i)))
				r += v.charAt(i);
			else
				return "";
		return r;
	}
		
	private static String getTrackNumber(String v) 
	{
		String r = "";
		for (int i=0; i<v.length(); i++)
		{
			char c = v.charAt(i);
			if (Character.isDigit(c)) 
				r+=c;
			//Break ved ikke tal, når vi er begyndt at samle tal.
			else
			{
				if (r.length()>0)
					break;
			}
		}
	
		if (r.length()>2) 
			r = "99"; //Ikke mere end 99!
		return r;
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