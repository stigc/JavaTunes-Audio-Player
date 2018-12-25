package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.AbstractTrack;

public interface IAudioPlayerHook
{
	public void audioStarting(AudioInfo audio);
	public void audioInterrupted(IAudio audio);
	public void audioFailed(IAudio audio, Exception ex);
	public void audioEnded(IAudio audio);
	public void tagsParsed(int sourceHashCode, AbstractTrack track);
}
