package dk.stigc.javatunes.audioplayer.tagreader;

public class TagId3 extends TagBase
{ 
	boolean fixErrorEncoding = false;
	String defaultCharset = "ISO-8859-1";
	
	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		fb.loadEnd();
		byte[] v = fb.buffer;			
		//TAG
		if (v[0]==0x54 && v[1]==0x41 && v[2]==0x47)
		{
			if (defaultCharset.length()==0)
			{
				title = new String(v, 3, 30);
				addArtist(new String(v, 33, 30));
				album = new String(v, 63, 30);
				addYear(new String(v, 93, 4));
			}
			else
			{
				title = new String(v, 3, 30, defaultCharset);
				addArtist (new String(v, 33, 30, defaultCharset));
				album = new String(v, 63, 30, defaultCharset);
				addYear(new String(v, 93, 4, defaultCharset));
			}					
			
			addTrackNumber((""+v[126]).trim());
			byte genreId = v[127];
			
			if (fixErrorEncoding)
			{
				title = Iso8859UtfFixer.fix(title);
				//artist = ISO8859_UTF_Fixer.fix(artist);
				album = Iso8859UtfFixer.fix(album);
			}
			
			if (genreId>=0 && genreId<Id3Genre.genres.length)
				addGenre(Id3Genre.genres[genreId]);

			tagFound= true;
		} 
	}
}


