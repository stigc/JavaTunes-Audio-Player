package dk.stigc.javatunes.audioplayer.player;

public interface IAudioPlayerHook
{
	public void audioStarting(AudioInfo audio);
	public void audioInterrupted(IAudio audio);
	public void audioFailed(IAudio audio, Exception ex);
	public void audioEnded(IAudio audio);
	
}
