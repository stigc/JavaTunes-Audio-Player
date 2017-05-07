package dk.stigc.javatunes.audioplayer.tagreader;

import java.io.*; 

public class TagApeV2 extends TagBase
{ 
	private String readItemIdentifier(byte[] data, int ptr) throws UnsupportedEncodingException
	{
		for (int i=0; i<256; i++)
		{
			if (data[ptr+i]==0)
				return new String (data, ptr, i, "US-ASCII");
		}
		
		return null;
	}
		
	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		fb.loadEnd();
		byte[] v = fb.buffer;
		int index = fb.readBytes - 32;
		//Log.write("fb.readBytes: " + fb.readBytes);
		if (v[index]==0x41 && v[index+1]==0x50 && v[index+2]==0x45
			&& v[index+3]==0x54 && v[index+4]==0x41 && v[index+5]==0x47
			&& v[index+6]==0x45 && v[index+7]==0x58)
		{
			index += 8;
			if (read32(v, index)==2000)
			{
				index += 4;
				int size = read32(v, index) + 32; //ex header
				index += 4;
				int tags = read32(v, index);
				
				//Log.write ("ApeV2");
				//Log.write ("size:" + size);
				//Log.write ("tags:" + tags);
				fb.loadEnd(size);
				//Log.write("load:" + fb.loadEnd(size));
				
				index = 32;
				for (int i=0; i<tags; i++)
				{
					int itemSize = read32(v, index);
					index+=8;
					String item = readItemIdentifier(v, index);
					item = item.toLowerCase();
					index += item.length() + 1; //+0byte
					String value = null;
					//Less than 10kb? This skips images.
					if (itemSize<1024*10)
						value = new String (v, index, itemSize, "UTF-8");
					//Log.write(item + "=" + value);
					//Log.write(item);
					
					
					if (item.equals("artist"))
						addArtist(value);
					else if (item.equals("album"))
						album = value;
					else if (item.equals("title"))
						title = value;
					else if (item.equals("track"))
						addTrackNumber(value);
					else if (item.equals("disc"))
						addDiscNumber(value);						
					else if (item.equals("genre"))
						addGenre(value);
					else if (item.equals("year"))
						addYear(value);
					else if (item.equals("replaygain_track_gain"))
						replaygain = getRG(value,0);
					else if (item.equals("replaygain_album_gain"))
						replaygainAlbumMode = getRG(value,0);
					else if (item.equals("cover art (front)"))
					{
						if (decodeImage)
						{
							int imgOffset = 0;
							for (int j=0; j<100; j++)
								if (v[index+j]==0)
								{
									imgOffset = j+1;
									break;
								}
									
							if (imgOffset>0)
							{
								imgData = new byte[itemSize-imgOffset];							
								System.arraycopy(v, index+imgOffset, imgData, 0, imgData.length);							
							}
						}
						embededCover = true;			
					}
					index += itemSize;
				}
				tagFound = true;
			}
		}
	}
}
	
	
