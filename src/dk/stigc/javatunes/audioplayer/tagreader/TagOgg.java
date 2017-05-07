package dk.stigc.javatunes.audioplayer.tagreader;

import dk.stigc.javatunes.audioplayer.other.*;

import java.io.*; 

public class TagOgg extends TagBase
{ 

	private static String tags[] = {"title=","artist=","album=","date=","genre=","tracknumber="
		, "replaygain_track_gain=", "replaygain_album_gain=", "lyrics="
			, "METADATA_BLOCK_PICTURE=", "coverart=", "DISCNUMBER="};
	private static byte[] commentsData;// = new byte[1024*1024];
	private static int commentsSize;
	private static int bufferIndex;

	private boolean isCovertag(int index)
	{
		return index==9 || index == 10;
	}
	protected String readTagName(byte[] data, int index) throws Exception
	{
		//30=MAX tag length
		for (int i=0; i<30; i++)
			if (data[index+i]==0x3d) //=
				return new String(data, index, i+1, "ISO-8859-1");
		return null;
	}
	
	private int seekOggS(byte[] v, int start, int end) throws Exception
	{
		//find "OggS"
		int length = end-4;
		for (int i=start; i<length; i++)
			if (v[i]==0x4F && v[i+1]==0x67 && v[i+2]==0x67 && v[i+3]==0x53)
				return i;
		return -1;
	}   	
	
	private void ensureCommentsData(byte[] data, FileBuffer fb, int indexToReadUntil)
	{
		while (indexToReadUntil > commentsSize)
		{
			//if (indexToReadUntil>1024)
			//	Log.write("Reading Next Page : " + bufferIndex);
			readNextHeader(data, fb);
		}
	}
		
	private void readNextHeader(byte[] data, FileBuffer fb)
	{
		//ensure bytes in buffer
		if (fb != null)
			fb.loadBuffer(bufferIndex + 28 + 255); //header + max segments count
		
		//Skip bytes and read segments count
		bufferIndex += 26;
		int segments = touint(data[bufferIndex]);
		bufferIndex++;

		//Find size of this page
		int	segmentsSize = 0;
		for (int i=0; i<segments; i++)
			segmentsSize += touint(data[bufferIndex+i]);
		bufferIndex += segments;

		//Log.write("segments:" + segments);
		//Log.write("index:" + index);
		//Log.write("segmentsSize:" + segmentsSize);
		//Log.write("commentsSize:" + commentsSize);

		//ensure segments data 
		if (fb != null)
			fb.loadBuffer(bufferIndex + segmentsSize);		
		//Log.write("copy data to : " + commentsSize + " length is " + segmentsSize);
		System.arraycopy(data, bufferIndex, commentsData, commentsSize, segmentsSize);
		commentsSize += segmentsSize;
		//return result;	
		bufferIndex += segmentsSize;
	}
	
	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		int startIndex = 4; //4 for at hoppe over 1. OggS
		
		byte[] data;
		if (commentsData==null)
			commentsData = new byte[1024*1024]; //1MB
		
		if (external==null)
		{
			data = fb.buffer;
		}
		else
		{
			data = external;				
		}
		
		commentsSize = 0;
		bufferIndex = seekOggS(data, startIndex, 1024);
		readAndParse(data, fb);
	}
	
	private void readAndParse(byte[] data, FileBuffer fb) throws Exception
	{
		//Reads first page
		ensureCommentsData(data, fb, 1);
		
		//Read comment header
		int index = 7; //skip .vorbis
		index += (int)toulong(read32(commentsData, index)); //skip Vendor_string
		index+=4;
		int comments = (int)toulong(read32(commentsData, index));
		index+=4;
		
		for (int i=0; i<comments; i++)
		{
			//read length
			ensureCommentsData(data, fb, index+4);
			int length = (int)toulong(read32(commentsData, index));
			index+=4;
			
			//read comment
			ensureCommentsData(data, fb, index+length);
			String tag = readTagName(commentsData, index);
			//Log.write("tag: " + tag);
			
			if (readTag(decodeImage, commentsData, tag, index, length))
				tagFound = true;
			index+=length;
		}	
	}
	
	protected boolean readTag(boolean decodeImage, byte[] data, String tag, int index, int length) throws UnsupportedEncodingException
	{
		if (tag==null)
			return false;
			
		for (int i=0; i<tags.length; i++)
		{
			if (StringFunc.startsWithIgnoreCase(tag, tags[i]))
			{
				boolean parseValue = !isCovertag(i) || (isCovertag(i) && decodeImage);
				//Log.write("tag:" +tag);
				String value = null;
				
				if (parseValue)
				{
					int s = index + tags[i].length();
					int l = length - tags[i].length();
					value = new String(data, s, l, "UTF-8");
					//Log.write("value:" + s + "-" + l);
					//Picture unpack
					if (isCovertag(i))
					{
						unpackPicture(value, i);
						return true;						
					}						
				}
				
				if (i==0) title = value;
				else if (i==1) addArtist(value);
				else if (i==2) album = value;
				else if (i==3) addYear(value);
				else if (i==4) addGenre(value);			
				else if (i==5) addTrackNumber(value);
				else if (i==6) replaygain = getRG(value,0);
				else if (i==7) replaygainAlbumMode = getRG(value,0);
				else if (i==8) lyrics = value;						
				else if (isCovertag(i)) embededCover = true;	
				else if (i==11) addDiscNumber(value);			
				//else if (i==11) 
				//	Log.write("Albumartist:" + value);
				return true;					
			}
		}				
		return false;	
	}

	private void unpackPicture(String base64picture, int index)
	{
		imgData = Base64.decode(base64picture);
		//COVERART= er pure img data and should not be unpacked
		if (index==9)
			unpackPicture(imgData);
	}
		
	protected void unpackPicture(byte[] data)
	{
		//Skipping metadata 
		//http://flac.sourceforge.net/format.html#metadata_block_picture
		int skip = 4;
		int mimeLength = (int)toulong(read32Reverse(data, skip));
		//Log.write("mimeLength:" + mimeLength);
		skip += 4;
		skip += mimeLength;
		int descLength = (int)toulong(read32Reverse(data, skip));
		//Log.write("descLength:" + descLength);
		skip += 4;
		skip += descLength;							
		skip += (5*4);							
		
		//build new array
		int newlength = data.length - skip;
		imgData = Common.resize(data, newlength, true); 
		//FileOutputStream fos = new FileOutputStream("c:\\out.jpg");
		//fos.write(imgData);
		//fos.write(commentsData, 0, commentsSize);
		//Log.write(value);
		//Log.write("l:" +l);	
	}
	
	final protected int touint(byte n) 
	{
		return (n & 0xff);
	}
	
	final protected long toulong(int n) 
	{
		return (n & 0xffffffffL);
	}
}