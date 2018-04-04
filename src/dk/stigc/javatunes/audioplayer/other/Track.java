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

	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Track [artists=");
		builder.append(artists);
		builder.append(", album=");
		builder.append(getAlbumFormated());				
		builder.append(", title=");
		builder.append(title);			
		builder.append(", genres=");
		builder.append(genres);	
		if (year>0)
		{
			builder.append(", year=");
			builder.append(getFormatedYear());
		}
		if (trackNumber>0)
		{
			builder.append(", trackNumber=");
			builder.append(getFormatedTrackNumber());
		}
		if (lyrics != null && lyrics.length()>0)
		{
			builder.append(", lyrics=");
			builder.append(lyrics);
		}
		if (codec != null)
		{
			builder.append(", codec=");
			builder.append(codec);
		}
		if (embededCover)
		{
			builder.append(", embededCover");
		}
//		if (isKaraoke)
//		{
//			builder.append(", karaoke");
//		}
		if (replaygain != REPLAY_GAIN_NOT_SET)
		{
			builder.append(", replaygain=");
			builder.append(replaygain);
		}
		if (replaygainAlbumMode != REPLAY_GAIN_NOT_SET)
		{
			builder.append(", replaygainAlbumMode=");
			builder.append(replaygainAlbumMode);
		}
		
		builder.append("]");
		return builder.toString();
	}

	public void copyFrom(Track that)
	{
		super.copyFrom(that);
		this.path = that.path;	
		this.lastModified = that.lastModified;		
	}
}
