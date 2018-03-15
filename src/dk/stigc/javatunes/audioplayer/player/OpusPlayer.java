package dk.stigc.javatunes.audioplayer.player;

import org.concentus.OpusDecoder;
import org.gagravarr.ogg.OggFile;
import org.gagravarr.opus.OpusAudioData;
import org.gagravarr.opus.OpusFile;

import dk.stigc.javatunes.audioplayer.other.Log;

public class OpusPlayer extends BasePlayer
{
	int granules;

	public OpusPlayer(int granules)
	{
		this.granules = granules;
	}

	@Override
	public void decode() throws Exception
	{
		OggFile ogg = new OggFile(bin);
		OpusFile of = new OpusFile(ogg);

		int lengthInSeconds = 0;
		
		if (granules > 0)
			lengthInSeconds = (int) (granules / 48000);

		audioInfo.setLengthInSeconds(lengthInSeconds);

//		int channels = of.getInfo().getNumChannels();
//		long rate = of.getInfo().getRate();
//		Log.write("channels " + channels);
//		Log.write("rate " + rate);

		// if ((Fs != 48000 && Fs != 24000 && Fs != 16000 && Fs != 12000 && Fs
		// != 8000)) {

		//OpusDecoder decoder = new OpusDecoder(48000, channels);
		//OpusDecoder decoder = new OpusDecoder(24000 * channels, 2);
		//initAudioLine(channels, 48000, 16, true, false);

		OpusDecoder decoder = new OpusDecoder(48000, 2);
		initAudioLine(2, 48000, 16, true, false);

		byte[] pcm = new byte[1024 * 128];

		OpusAudioData ad = of.getNextAudioPacket();
		
		while (running && ad != null)
		{
			byte[] data = ad.getData();

			int samplesDecoded = decoder.decode(data, 0, data.length, pcm, 0, pcm.length, false);

			write(pcm, samplesDecoded * 4);
			
			ad = of.getNextAudioPacket();
		}
	}
}
