package dk.stigc.javatunes.audioplayer.other;

import dk.stigc.javatunes.audioplayer.player.IAudio;

public class Track extends AbstractTrack implements IAudio
{
	public String path;
	public long lastModified;
	
	@Override
	public double getReplayGain(boolean albumMode)
	{
		if (albumMode && replaygainAlbumMode != Track.REPLAY_GAIN_NOT_SET)
			return replaygainAlbumMode;
		if (replaygain != Track.REPLAY_GAIN_NOT_SET)
			return replaygain;
		return 0;
	}

	@Override
	public String getPath()
	{
		return this.path;
	}

	@Override
	public Codec getCodec()
	{
		return codec;
	}

	public String toString()
	{
		String r = "Artist: " + getArtist() + "\nAlbum: " + getAlbumFormated() + "\nTitle: "
				+ title + "\nTrack: " + getFormatedTrackNumber() + "\nYear: "
				+ getFormatedYear() + "\nGenre: " + getGenre() + "\nLocation: "
				+ path + "\n";

		return r;
	}

	public String getFormatedTrackNumber()
	{
		if (trackNumber == 0)
			return "";
		else if (trackNumber < 10)
			return "0" + trackNumber;
		else
			return "" + trackNumber;
	}

	public String getAlbumFormated()
	{
		if (discNumber > 1)
			return album + " (DISC " + discNumber + ")";
		return album;
	}

	public void copyFrom(Track that)
	{
		super.copyFrom(that);
		this.path = that.path;	
		this.lastModified = that.lastModified;		
	}
}
