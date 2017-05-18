package dk.stigc.javatunes.audioplayer.other;

public abstract class AbstractTrack
{
	public int year, trackNumber, discNumber;
	public String title = "", album = "", lyrics = "";
	public StringList artists = new StringList();
	public StringList genres = new StringList();

	public Codec codec;
	public boolean embededCover;
	public static double REPLAY_GAIN_NOT_SET = Double.MIN_VALUE;
	public double replaygain = REPLAY_GAIN_NOT_SET;
	public double replaygainAlbumMode = REPLAY_GAIN_NOT_SET;

	public String getFormatedYear()
	{
		if (year==0)
			return "";
		return "" + year;
	}

	public String getGenre()
	{
		return genres.toString(" / ");
	}
	
	public String getArtist()
	{
		return artists.toString(" / ");
	}

	public void copyFrom(AbstractTrack that)
	{
		this.artists = new StringList(that.artists);
		this.genres = new StringList(that.genres);
		this.title = that.title;	
		this.album = that.album;
		this.year = that.year;
		this.trackNumber = that.trackNumber;
		this.discNumber = that.discNumber;
		this.embededCover = that.embededCover;
		this.replaygain = that.replaygain;
		this.replaygainAlbumMode = that.replaygainAlbumMode;
		this.lyrics = that.lyrics;
		this.codec = that.codec;
	}

	public void clear()
	{
		replaygain = REPLAY_GAIN_NOT_SET;
		replaygainAlbumMode = REPLAY_GAIN_NOT_SET;
		title = "";
		album = "";
		artists.clear();
		genres.clear();		
		year = 0;
		trackNumber = 0;
		discNumber = 0;
		embededCover = false;
		lyrics = "";
		codec = null;
	}	
  	
	public boolean hasReplayGain()
  	{
  		return replaygain!=REPLAY_GAIN_NOT_SET || replaygainAlbumMode!=REPLAY_GAIN_NOT_SET;
  	}
  	
	public boolean isTheSame(AbstractTrack o)
	{
		return this.album.equals(o.album)
			&& this.artists.isTheSame(o.artists)
			&& this.title.equals(o.title)
			&& this.year==o.year
			&& this.genres.isTheSame(o.genres)
			&& this.trackNumber==o.trackNumber
			&& this.discNumber==o.discNumber
			&& this.embededCover==o.embededCover
			&& this.replaygain==o.replaygain
			&& this.replaygainAlbumMode==o.replaygainAlbumMode;
	}
}