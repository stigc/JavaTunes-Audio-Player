package dk.stigc.javatunes.audioplayer.other;


public enum Codec
{
	unknown(""),
	mp3("MP3"),
	vorbis("Ogg Vorbis"),
	flac("FLAC"),
	wavpack("WavPack"),
	aac("AAC"),
	aacadts("AAC ADTS"),
	alac("Apple Lossless"),
	opus("Opus"),
	//containers
	mp4container("MP4"), //transforms -> aac, alac
	ogg("Ogg") //transforms -> aac, alac
	;
	
    public String name;

    Codec(String name)
    {
        this.name = name;
    }   

	public static Codec extractCodecFromExtension(String filePath)
	{
		if (StringFunc.endsWithIgnoreCase(filePath, ".mp3"))
			return mp3;
		if (StringFunc.endsWithIgnoreCase(filePath, ".ogg"))
		 	return vorbis;
		if (StringFunc.endsWithIgnoreCase(filePath, ".flac"))		
			return flac;
		if (StringFunc.endsWithIgnoreCase(filePath, ".fla"))		
			return flac;
		if (StringFunc.endsWithIgnoreCase(filePath, ".wv"))
			return wavpack;
		if (StringFunc.endsWithIgnoreCase(filePath, ".mp4"))
			return mp4container;
		if (StringFunc.endsWithIgnoreCase(filePath, ".m4a"))
			return mp4container;						
		if (StringFunc.endsWithIgnoreCase(filePath, ".aac"))
			return aacadts;		
		if (StringFunc.endsWithIgnoreCase(filePath, ".opus"))
			return opus;	
		return unknown;
	}

	public static boolean isCodecSupportedFromExtension(String filePath)
	{
		return extractCodecFromExtension(filePath)!=unknown;
	}
	
}
