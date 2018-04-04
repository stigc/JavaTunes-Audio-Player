package dk.stigc.javatunes.audioplayer.tagreader;

import dk.stigc.common.StringFunc;
import dk.stigc.javatunes.audioplayer.other.*;
import java.util.*; 

/*
4.   ID3v2 frame overview

   All ID3v2 frames consists of one frame header followed by one or more
   fields containing the actual information. The header is always 10
   bytes and laid out as follows:

     Frame ID      $xx xx xx xx  (four characters)
     Size      4 * %0xxxxxxx
     Flags         $xx xx

   The frame ID is made out of the characters capital A-Z and 0-9.
   Identifiers beginning with "X", "Y" and "Z" are for experimental
   frames and free for everyone to use, without the need to set the
   experimental bit in the tag header. Bear in mind that someone else
   might have used the same identifier as you. All other identifiers are
   either used or reserved for future use.

   The frame ID is followed by a size descriptor containing the size of
   the data in the final frame, after encryption, compression and
   unsynchronisation. The size is excluding the frame header ('total
   frame size' - 10 bytes) and stored as a 32 bit synchsafe integer.

   In the frame header the size descriptor is followed by two flag
   bytes. These flags are described in section 4.1.

   There is no fixed order of the frames' appearance in the tag,
   although it is desired that the frames are arranged in order of
   significance concerning the recognition of the file. An example of
   such order: UFID, TIT2, MCDI, TRCK ...

   A tag MUST contain at least one frame. A frame must be at least 1
   byte big, excluding the header.

   If nothing else is said, strings, including numeric strings and URLs
   [URL], are represented as ISO-8859-1 [ISO-8859-1] characters in the
   range $20 - $FF. Such strings are represented in frame descriptions
   as <text string>, or <full text string> if newlines are allowed. If
   nothing else is said newline character is forbidden. In ISO-8859-1 a
   newline is represented, when allowed, with $0A only.

   Frames that allow different types of text encoding contains a text
   encoding description byte. Possible encodings:
	
     $00   ISO-8859-1 [ISO-8859-1]. Terminated with $00.
     $01   UTF-16 [UTF-16] encoded Unicode [UNICODE] with BOM. All
           strings in the same frame SHALL have the same byteorder.
           Terminated with $00 00.
     $02   UTF-16BE [UTF-16] encoded Unicode [UNICODE] without BOM.
           Terminated with $00 00.
     $03   UTF-8 [UTF-8] encoded Unicode [UNICODE]. Terminated with $00.
     
     (denne byte er den første efter de 10 bytes )

*/
public class TagId3V2  extends TagBase
{ 
	//http://id3lib.sourceforge.net/id3/id3v2.4.0-changes.txt	
	//TDRC udskifter TYER i v2.4
	public int version;	
	String tags_v3[] = {"TIT2","TPE1","TALB","TYER","TCON","TRCK","TPOS","APIC","TXXX","TDRC"}; //, "USLT"};
	String tags_v2[] = {"TT2","TP1","TAL","TYE","TCO","TRK"}; //, "USLT"};
	byte[] v;
	boolean fixErrorEncoding = false;
	String defaultCharset = "ISO-8859-1";
	
	static byte tags_as_bytes_v2[][];
	static byte tags_as_bytes_v3[][];
	
	private byte[][] init(String tags[]) throws Exception
	{	
		int length = tags.length;
		byte tagsAsBytes[][] = new byte[length][];
		for (int i=0; i<length; i++)
		{
			String tag = tags[i];
			byte b[] = tag.getBytes("ISO-8859-1");
			int tagLength = b.length;
			tagsAsBytes[i] = new byte[tagLength];
			for (int j=0; j<tagLength; j++)
				tagsAsBytes[i][j] = b[j];
		}
		return tagsAsBytes;
	}
	
	private ArrayList<String> values = new ArrayList<String>();
	private String extractString(int index, int length, int maxBuffer, int encoding) throws Exception
	{
		//UTF-8 is only supported in ID3v2.4 (UCS-2 is used in 2.2 and 2.3, 
		//but they differ, 2.3 requires a byte order marker and 2.2 assumes big endian). 
		//Most text frames have an additional byte after the header which specifies the encoding: 			
		//0x00 is ISO-8859-1 (in other words ASCII, to be simplistic)
		//0x01 is UCS-2 in 2.2 an 2.3 (note differences), UTF-16 with BOM in 2.4
		//0x02 is UTF-16BE in 2.4 only
		//0x03 is UTF-8 in 2.4 only
		
		//Bytes Encoding Form 
		
		
		//BOM MARKS
		//00 00 FE FF 		UTF-32, big-endian 
		//FF FE 00 00 		UTF-32, little-endian 
		//FE FF 			UTF-16, big-endian 
		//FF FE 			UTF-16, little-endian 
		//EF BB BF 			UTF-8 
		
		//if (encoding>0)
		//	Log.write ("Encoding: " +  encoding);
		
		values.clear();
		boolean wide = false;		
		if(index+length>=maxBuffer) return "";
		
		String text = "";
		String charset = null;
		
		//NB! Bytes is signed
		//BOM detected!
		if (v[index]==-1 && v[index+1]==-2 || v[index]==-2 && v[index+1]==-1) 
		{
			charset = "UTF-16"; //UTF-16 Uses (and removes) the BOM
			wide = true;
		}
		else if (encoding==1)
		{
			//UCS-2 id3v2.3
			//Log.write("UCS-2 not supported. Use UTF-16");
			charset = "UTF-16"; //Version 2.2 with no BOM
			wide = true;
		}
		else if (encoding==2)
		{
			charset = "UTF-16BE"; //Version 2.4 with no BOM
			wide = true;
		}
		else if (encoding==3)
			charset = "UTF-8"; 	//Version 2.4 with no BOM
		else
			charset = defaultCharset; //defaultCharset is null if not set.

		//Log.write("charset: " + charset);
		//Flere værdier i samme tag. Sæt "/" seperator ind.
		//Søg \0 string termineringer.
		//boolean removeDharmaWheel = false;
		int l = index+length;
		int step = wide ? 2 : 1;
		
		int pointer = index;
		
		for (int i=index; i<l; i+=step)
		{
			boolean nullbyte = v[i]==0 && (!wide || v[i+1]==0);
			boolean lastbyte = i == l-step;
				
			if (nullbyte || lastbyte)
			{
				int tokenlength = i-pointer;
				
				if (lastbyte)
					tokenlength += step;
				
				//Log.write(":");
				String tokentext = (charset.length()==0) ?
						new String(v, pointer, tokenlength)
						: new String(v, pointer, tokenlength, charset);	
				
				tokentext = tokentext.trim();
				
				if (tokentext.length()>0)
				{
					values.add(tokentext);
					text = tokentext;
					//if (text.length()>0)
					//	text += " / ";
					//text += tokentext;
				}
				
				pointer = i + step;
			}
		}

		if (fixErrorEncoding && charset.equals("ISO-8859-1"))
			text = Iso8859UtfFixer.fix(text);
		return text;
	}

	//Leder efter "\0\0TAG="
	private int seekTag(byte[] data, int start, int bufferLoaded, byte[] b) throws Exception
	{
		//Log.write("b1: " + b1.length);
		int length = bufferLoaded-32;
		for (int i=start; i<length; i++)
		{
			for (int j=0; j<b.length; j++)
			{
				if (data[i+j]!=b[j])
					break;
				else if (j==b.length-1)
					return i;
			}
		}
		return -1;
	}

	public static int deUnsynchronize(byte[] bytes, int start, int end)
	{
		//Log.write("de-unsynchronisation " + start + " to " + end);
		int minus = 0;
		for (int i=start; i<end-1; i++)
		{
			if (bytes[i]==(byte)0xFF && bytes[i+1]==(byte)0x00)
			{
				//arraycopy(Object src, int srcPos, Object dest, int destPos, int length) 
				System.arraycopy(bytes, i+1, bytes, i, end-(i+1));
				bytes[i]=(byte)0xFF;
				minus++;
			}
 		} 
 		return minus;			
	}
	
	//http://www.id3.org/id3v2.4.0-structure.txt
	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		v = external;
		if (v==null)
			v = fb.buffer;
			
		int bufferLoaded = v.length;


		//ID3
		if (v[0]==0x49 && v[1]==0x44 && v[2]==0x33)
		{
			version = v[3];	
			if (version!=2 && version!=3 && version!=4) return;
			
			if (tags_as_bytes_v2 == null)
			{
				tags_as_bytes_v2 = init(tags_v2);
				tags_as_bytes_v3 = init(tags_v3);
			}
			
			int length = tags_v3.length;
			byte tagsAsBytes[][] = tags_as_bytes_v3;
			if (version==2) 
			{
				tagsAsBytes = tags_as_bytes_v2;
				length = tags_v2.length;
			}
			
			//Tag framesize
			int alltagssize = synchSafeBytesToInt(v, 6) + 10;

			//hent mere fra fil
			if (fb!=null)
				bufferLoaded = fb.ensureBufferLoad(alltagssize);
			
			for (int i=0; i<length; i++) 
			{
				int index  = 0;
				int size = 0;
				int encoding = 0;
				boolean multipleTag = i==8;
			
				do
				{
					index = seekTag(v, index, bufferLoaded, tagsAsBytes[i]);
					//Log.write(tags_v3[i] + ":" + index);
					
					if (index!=-1) 
					{
						if (version==2)
						{
							encoding = v[index+6]; //Holds the encoding.
							size = read24Reverse(v, index+3)-1;
							index += 7;
						}
						
						else
						{
							if (version==3)
								size = read32Reverse(v, index+4)-1;
							else
								size = synchSafeBytesToInt(v, index+4)-1;
							
							//See 02 - Baby Can I Hold You.mp3
							//http://www.id3.org/id3v2.4.0-structure
							//See 4.1.   Frame header flags
							boolean dli = (v[index+9] & 1) > 0;
							boolean us = (v[index+9] & 2) > 0;
							boolean gi = (v[index+9] & 64) > 0;
							if (version<4)
								gi = (v[index+9] & 32) > 0;
								
							/*
							if (dli || us || gi)
							{
								Log.write("flag set: " + dli);		
								Log.write("flag set: " + us);
								Log.write("flag set: " + gi);
							}
							*/										
																		
							if (dli)
							{
								//Log.write("!!!");
								index+=4;
								size-=4;
							}
																							
							encoding = v[index+10]; //Holds the encoding.

							if (us)
							{
								int dsIndex = index+11;
								//Only deUnsynchronize if buffer is big enough -> "C:\\Musik\\Camille Jones\\01 - The Creeps.mp3";
								if (dsIndex+size < bufferLoaded)
								{
									int minus = deUnsynchronize(v, dsIndex, dsIndex+size);
									size -= minus;
								}
							}	
																	
							index += 11;
						}
	
						if(index+size<bufferLoaded && size>0)
						{
							tagFound = true;
							if (i==7)
							{
								embededCover = true;
								if (decodeImage)
									decodeImage(index, size);
							}
							else
							{
								
								String tmp = extractString(index, size, bufferLoaded, encoding);
								//Log.write("index:" + index);
								//Log.write("tmp:" + tmp);
								
								if (i==0) title = tmp;
								else if (i==1)
								{ 
									for(String s: values)
										addArtist(s);
								}
								else if (i==2) album = tmp;
								else if (i==3 || i==9) addYear(tmp);
								else if (i==4) 
								{
									for(String s: values)
										addGenre(getGenre(s));
								}
								else if (i==5) addTrackNumber(tmp);
								else if (i==6) addDiscNumber(tmp);
								else if (i==8) 
								{
									if (values.size()>1)
									{
										String identifier = values.get(0);
										String value = values.get(1);
										//Log.write(identifier + ";" + value);
										if (StringFunc.startsWithIgnoreCase(identifier, "replaygain_track_gain"))
											replaygain = getRG(value,0);
										else if (StringFunc.startsWithIgnoreCase(identifier, "replaygain_album_gain"))
											replaygainAlbumMode = getRG(value,0);
										else if (StringFunc.startsWithIgnoreCase(identifier, "lyrics"))
											lyrics = value;

									}
								}								
							}
						}
					}
				}
				while (multipleTag && index>-1);
			}
		}
	}
	
	private void decodeImage(int index, int size)
	{
		//<Header for 'Attached picture', ID: "APIC">
		//Text encoding      $xx
		//MIME type          <text string> $00
		//Picture type       $xx
		//Description        <text string according to encoding> $00 (00)
		//Picture data       <binary data>
		
		int nextNull = index;
		while (v[nextNull]!=0)
		nextNull++;
		int picindex = nextNull + 1;
		int picturetype = v[picindex];
		nextNull = picindex + 1;
		while (v[nextNull]!=0)
		nextNull++;	
		picindex = nextNull + 1;									
		
		int picsize = size-(picindex-index);
		imgData = new byte[picsize];
		//arraycopy(Object src, int srcPos, Object dest, int destPos, int length) 
		System.arraycopy(v, picindex, imgData, 0, picsize);		
	}
	
	private String getGenre(String v) 
	{
		int i1 = v.indexOf("(");
		int i2 = v.indexOf(")");
		
		if (i1>-1 && i2>-1)
		try
		{	
			String number = v.substring(i1+1,i2);
			return Id3Genre.genres[Integer.parseInt(number)];
		}
		catch(Exception ex){}
		return v;
	}	
}