package dk.stigc.javatunes.audioplayer.player;

import dk.stigc.javatunes.audioplayer.other.*;

public interface IAudio
{
	String getPath();
	Codec getCodec();
	double getReplayGain(boolean albumMode);
}
