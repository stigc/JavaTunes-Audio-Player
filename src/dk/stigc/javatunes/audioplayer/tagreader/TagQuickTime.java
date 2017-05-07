package dk.stigc.javatunes.audioplayer.tagreader;

import java.util.Arrays;
import java.util.List;

import dk.stigc.javatunes.audioplayer.other.Codec;

public class TagQuickTime extends TagBase
{ 
	public static final List<String> containers 
		//"cmvd", "stbl",
		= Arrays.asList("moov","trak","clip","matt","edts","tref","mdia","minf","dinf","udta","cmov","rmra","rmda","gmhd","ilst");
	
	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		byte[] v = fb.buffer;
		fb.loadBuffer(1024*1024);
		readSubAtom(v, 0, fb.readBytes, 0, "");
	}
	
	private String GetString(byte[] v, int from, int length) throws Exception
	{
		return new String(v, from, length, "UTF-8");
	}
	private void readSubAtom(byte[] v, int offset, int length, int level, String hierarchy) throws Exception
	{
		String boxTypeName = "";
		int index = offset;
		while (index<offset+length)
		{
			int subAtomSize = read32Reverse(v, index);
			String subAtomName = new String(v, index+4, 4, "ISO-8859-1");
			/*Log.write
			(
					new String(new char[level]).replace("\0", " ")
					+ " subAtom: " + subAtomName + " : " + subAtomSize + " # " + hierarchy
			);*/
			
			if (containers.contains(subAtomName))
				readSubAtom(v, index+8, subAtomSize-8, level+1, hierarchy + "/" + subAtomName);
			else if (subAtomName.equals("meta"))
				readSubAtom(v, index+8+4, subAtomSize-8-4, level+1,  hierarchy + "/" + subAtomName);
			else if (subAtomName.equals("stbl"))
				readSubAtom(v, index+8, subAtomSize-8, level+1,  hierarchy + "/" + subAtomName);		
			else if (subAtomName.equals("stsd"))
			{
				//1 bytes: version
				//3 bytes: flags_raw
				//4 bytes: number_entries
				int itemsCount = read32Reverse(v, index+8+4);
				String dataFormat = GetString(v, index+8+4+4+4, 4);
				if (codec == null)
				{
					if (dataFormat.equals("alac"))
						codec = Codec.alac;
					else 
						codec = Codec.aac;
					//Log.write("codec set to : " + codec);
				}
				//Log.write("itemsCount: " + itemsCount);
			}
			else if (hierarchy.endsWith("/moov/trak/mdia") && subAtomName.equals("mdhd"))
			{
				int timeScale = read32Reverse(v, index+12+8);
				int duration = read32Reverse(v, index+16+8);
				//Log.write("XXX:" + (duration/timeScale));
			}
			//Read the "data" atom of "meta/ilst"
			else if (hierarchy.endsWith("meta/ilst"))
				readSubAtom(v, index+8, subAtomSize-8, level+1,  hierarchy + "/" + subAtomName);
			else if (hierarchy.endsWith("ilst/©ART"))
			{
				addArtist(GetString(v, index+8+8, subAtomSize-8-8));
				tagFound= true;
			}
			else if (hierarchy.endsWith("ilst/©nam"))
				title = GetString(v, index+8+8, subAtomSize-8-8);
			else if (hierarchy.endsWith("ilst/©alb"))
				album = GetString(v, index+8+8, subAtomSize-8-8);
			else if (hierarchy.endsWith("ilst/trkn"))
			{
				int track = read16Reverse(v, index+18);
				addTrackNumber(""+track);
			}
			else if (hierarchy.endsWith("ilst/©gen"))
				addGenre(GetString(v, index+8+8, subAtomSize-8-8));
			else if (hierarchy.endsWith("ilst/gnre"))
			{
				int genre = read32Reverse(v, index+14) - 1;
				addGenre(Id3Genre.genres[genre]);
			}
			else if (hierarchy.endsWith("ilst/©day"))
				addYear(GetString(v, index+8+8, subAtomSize-8-8));			
			else if (hierarchy.endsWith("ilst/----"))
			{
				String boxType =  new String(v, index+4, 4);
				if (boxType.equals("name"))
				{
					boxTypeName = GetString(v, index+8+4, subAtomSize-8-4);
					//Log.write("name: " + boxTypeName);
				}
				else if (boxType.equals("data"))
				{
					String text = GetString(v, index+8+8, subAtomSize-8-8);
					if (boxTypeName.equals("replaygain_track_gain"))
						replaygain = getRG(text, 0);
					if (boxTypeName.equals("replaygain_album_gain"))
						replaygainAlbumMode = getRG(text, 0);						
					//Log.write("data: " + text);
				}
			}
			index+=subAtomSize;			
		}
	}
}
	
	
