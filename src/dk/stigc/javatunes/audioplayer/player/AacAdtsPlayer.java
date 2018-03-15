package dk.stigc.javatunes.audioplayer.player;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class AacAdtsPlayer extends BasePlayer
{
    public void decode() throws Exception
    {
    	boolean init = false;
    	byte[] b;
    	final ADTSDemultiplexer adts = new ADTSDemultiplexer(bin);
		final Decoder dec = new Decoder(adts.getDecoderSpecificInfo());
		final SampleBuffer buf = new SampleBuffer();
		
		while(true) 
		{
			b = adts.readNextFrame();
			dec.decodeFrame(b, buf);

			audioInfo.addVariableBitrate(buf.getEncodedBitrate());
			
			if(!init)
			{
				initAudioLine(buf.getChannels(), buf.getSampleRate(), buf.getBitsPerSample(), true, true);	
				init = true;
			}

			if (!running) 
				return;
				
			b = buf.getData();
			//Log.write("b " + b.length + " : " + b[0]);
			write(b, b.length);
		}
    }
}