package dk.stigc.javatunes.audioplayer.tagreader;

import dk.stigc.javatunes.audioplayer.other.AbstractTrack;
import dk.stigc.javatunes.audioplayer.other.Log;

public class BufferTagReader
{
	private static boolean isHeader(byte[]b, String tag)
	{
		for (int i=0; i<tag.length(); i++)
		{	
			char c1 = Character.toLowerCase(tag.charAt(i));
			char c2 = Character.toUpperCase(c1);
			if (b[i]!=c1 && b[i]!=c2) return false;
		}
		
		return true;
	}
	
	public static AbstractTrack Parse(byte[] data)
	{
		TagBase tr = null;
		
		if (isHeader(data, "ID3"))
			tr = new TagId3V2();
		if (isHeader(data, "OggS"))
			tr = new TagOgg();
		else
			tr = new TagFlac();

		if (tr.parse(null, data, false) == false)
			return null;
		
		return tr;
	}
}
