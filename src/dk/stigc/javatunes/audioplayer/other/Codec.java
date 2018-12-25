package dk.stigc.javatunes.audioplayer.other;

import dk.stigc.common.StringFunc3;

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
	oggcontainer("Ogg"), //transforms -> vorbis, opus
	hlc("HLC") 
	;
	
    public String name;

    Codec(String name)
    {
        this.name = name;
    }   

	public static Codec extractCodecFromExtension(String filePath)
	{
		if (StringFunc3.endsWithIgnoreCase(filePath, ".mp3"))
			return mp3;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".ogg"))
		 	return vorbis;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".flac"))		
			return flac;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".fla"))		
			return flac;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".wv"))
			return wavpack;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".mp4"))
			return mp4container;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".m4a"))
			return mp4container;						
		if (StringFunc3.endsWithIgnoreCase(filePath, ".aac"))
			return aacadts;		
		if (StringFunc3.endsWithIgnoreCase(filePath, ".opus"))
			return opus;
		if (StringFunc3.endsWithIgnoreCase(filePath, ".ts"))
			return hlc;		
		return unknown;
	}

	public static boolean isCodecSupportedFromExtension(String filePath)
	{
		return extractCodecFromExtension(filePath)!=unknown;
	}
}
