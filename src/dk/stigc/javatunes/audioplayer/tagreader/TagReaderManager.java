package dk.stigc.javatunes.audioplayer.tagreader;

import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*; 

public class TagReaderManager
{ 
	public int noTag;
	public byte[] imgData;
	private FileBuffer fb = new FileBuffer();
	
	TagId3 o1 = new TagId3();
	TagId3V2 o2 = new TagId3V2();
	TagOgg o3 = new TagOgg();
	TagFlac o4 = new TagFlac();
	TagApeV2 o5 = new TagApeV2();
	TagQuickTime o6 = new TagQuickTime();
	TagFromFileName tagFromFileName = new TagFromFileName();
	NoTagReader noTagReader = new NoTagReader();

	TagBase[] noTagReades = new TagBase[] {noTagReader};
	TagBase[] mp3TagReades = new TagBase[] {o2,o1};
	TagBase[] vorbisTagReades = new TagBase[] {o3};
	TagBase[] flacTagReades = new TagBase[] {o4,o2,o1};
	TagBase[] wavpackTagReades = new TagBase[] {o5,o1};
	TagBase[] quickTimeTagReades = new TagBase[] {o6};
	
	private TagBase[] getTagReaders(Codec codec)
	{
		switch (codec)
		{	
			case mp3: return mp3TagReades;
			case vorbis: return vorbisTagReades;
			case flac: return flacTagReades;
			case wavpack: return wavpackTagReades;
			case mp4container: return quickTimeTagReades;
			default: return	noTagReades;
		}
	}
    
    public Track read(File f) throws FileNotFoundException
    {
    	return read(f, false);
    }
            	    
    public synchronized Track read(File file, boolean decodeImage) throws FileNotFoundException
    {
    	String path = file.getAbsolutePath();
    	Codec codec = Codec.extractCodecFromExtension(path);
    	
    	if (codec!=Codec.unknown)
		{
    		fb.setFile(file);
    		
			TagBase tagReader = null;
			
			for(TagBase nextTagReader: getTagReaders(codec))
			{
				tagReader = nextTagReader;
				if (tagReader.parse(fb, decodeImage))
				{
					if (decodeImage)
						imgData = tagReader.imgData;
					break;
				}
			}
			
			fb.close();
			
			//No tags parsed, use TagFromFileName instead.
			if (!tagReader.tagFound)
			{
				noTag++;
				tagFromFileName.path = path;
				tagFromFileName.parse(null, false);
				//keep codec read from mp4 container
				tagFromFileName.codec = tagReader.codec; 
				tagReader = tagFromFileName;
			}

			Track track = new Track();
			track.lastModified = file.lastModified();
			track.copyFrom(tagReader);
			track.path = path;
			//tag parsing did not extract any codec.
			if (track.codec == null)
				track.codec = codec;
			return track;
		}
    	
		return null;
	}
}