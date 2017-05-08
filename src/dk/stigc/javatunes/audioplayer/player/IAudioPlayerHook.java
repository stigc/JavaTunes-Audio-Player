package dk.stigc.javatunes.audioplayer.player;

public interface IAudioPlayerHook
{
	public void trackDecodingError(Exception ex);
	public void trackEnded(boolean finished);
}
