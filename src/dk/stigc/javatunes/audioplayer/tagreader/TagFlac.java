package dk.stigc.javatunes.audioplayer.tagreader;

public class TagFlac extends TagOgg
{ 
	private int commentsStart;
	private int commentsSize;
	private int pictureStart;
	private int pictureSize;

	private void findMetaDataBlocks(byte[] data, FileBuffer fb)
	{
		fb.ensureBufferLoad(8); //header+streamInfoblock
		
		//Skip mandatory streamInfo block
		int skip = 4;
		byte headerFirstByte = data[skip];
		skip++;
		int streamInfoLength = (int)toulong(read24Reverse(data, skip));
		skip += 3;
		skip += streamInfoLength;
		
		//Last-metadata-block flag: 
		//'1' if this block is the last metadata block before the audio blocks, '0' otherwise.
		while ((headerFirstByte & 128) != 128)
		{	
			//Prepare read of header
			fb.ensureBufferLoad(skip+1+3);
			
			//Read first byte
			//Log.write("skip: " + skip);
			headerFirstByte = data[skip];
			skip++;
			
			//Check for meta type
			byte b = (byte)(headerFirstByte & 127);
			boolean isPicture = (b == 6);
			boolean isComments = (b == 4);

			int length = (int)toulong(read24Reverse(data, skip));
			skip+=3;
			
			if (isComments)
			{
				commentsStart = skip;
				commentsSize = length;
			}
						
			if (isPicture)
			{
				pictureStart = skip;
				pictureSize = length;
			}
			
			skip += length;
			
			if (commentsSize>0 && pictureSize>0)
				break;
		}
	}
	
	public void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		byte[] v;
		
		if (external==null)
		{
			v = fb.buffer;
		}
		else
		{
			v = external;				
		}
		
		//fLaC
		if (v[0]!=0x66 || v[1]!=0x4c || v[2]!=0x61 || v[3]!=0x43)
			return;
	
		commentsStart = pictureStart = commentsSize = pictureSize = 0;
		
		findMetaDataBlocks(v, fb);
		//Log.write("Comments: " + commentsStart + " - " + commentsSize);
		//Log.write("Picture: " + pictureStart + " - " + pictureSize);

		if (commentsSize>0)
		{
			//Ensure data
			fb.ensureBufferLoad(commentsStart+commentsSize);
			
			//Read comment header
			int index = commentsStart;
			index += (int)toulong(read32(v, index)); //skip Vendor_string
			index+=4;
			int comments = (int)toulong(read32(v, index));
			index+=4;
			
			//Log.write("comments:" + comments);
			for (int i=0; i<comments; i++)
			{
				//read length
				fb.ensureBufferLoad(index+4);
				int length = (int)toulong(read32(v, index));
				index+=4;
				
				//read comment
				String tag = readTagName(v, index);
				if (readTag(decodeImage, v, tag, index, length))
					tagFound = true;
					
				index+=length;						
			}
		}
		
		if (pictureSize>0)
		{
			if (decodeImage)
			{
				//Ensure data
				fb.ensureBufferLoad(pictureStart+pictureSize);
				byte[] img = new byte[pictureSize];
				//arraycopy(Object src, int srcPos, Object dest, int destPos, int length) 
				System.arraycopy(v, pictureStart, img, 0, pictureSize);	
				unpackPicture(img);
				//FileOutputStream fos = new FileOutputStream("c:\\out.jpg");
				//fos.write(imgData);
			}
			
			embededCover = true;
		}
	}
}