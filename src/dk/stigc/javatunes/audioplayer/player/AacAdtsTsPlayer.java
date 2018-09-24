package dk.stigc.javatunes.audioplayer.player;

import java.io.IOException;
import java.io.InputStream;

import dk.stigc.javatunes.audioplayer.other.Log;
import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.adts.ADTSDemultiplexer;

public class AacAdtsTsPlayer extends BasePlayer
{
    public void decode() throws Exception
    {
    	boolean init = false;
    	byte[] b;
    	final ADTSDemultiplexer adts = new ADTSDemultiplexer(new TsInputstream(bin));
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

class TsInputstream extends InputStream
{

	public TsInputstream(InputStream source)
	{
		super();
		this.source = source;
	}

	InputStream source;
	byte[] currentTsPacket;
	int currentIndex;
	
	private byte read2() throws IOException
	{
		byte b = (byte)source.read();
		return b;	
	}
	
	@Override
	public int read() throws IOException
	{
		while (currentTsPacket == null || currentIndex == currentTsPacket.length)
		{			
			currentTsPacket = readNextPacket();
			currentIndex = 0;
		}
		
		byte b = currentTsPacket[currentIndex];
		currentIndex++;
		return 0x00 << 24 | b & 0xff;
	}

	private byte[] readNextPacket() throws IOException
	{
		byte g = read2();
		//Log.write("G is " + g);
		
		byte flags = read2();
		byte flags2 = read2();
		byte flags3 = read2();
		
		int tei = flags & 0x0001;
		int psi = (flags >> 1) & 0x0001;
		int tp = (flags >> 2) & 0x0001;
		int pid = ((flags << 8) | (flags2 & 0xff)) & 0x1fff;
		int scramblingControl = (flags3 >> 6) & 0x03;
		boolean hasAdaptationField = (flags3 & 0x20) != 0;
		boolean hasPayloadData  = (flags3 & 0x10) != 0;
		int continuityCount = flags3 & 0x0f;
		
		int bytesLeft = 188-4;
		
		if (hasAdaptationField)
		{
			int length = read2() & 0xff;
			//Log.write("length:_" + length);
			bytesLeft--;
			
			if (bytesLeft - length < 0)
				throw new RuntimeException("Overrun");

			for (int i=0; i<length; i++)
				read2();
			bytesLeft -= length;
		}
		
		if (bytesLeft <= 0)
			return null;
		
		byte[] tsPacker = new byte[bytesLeft];
		
		for (int i=0; i<bytesLeft; i++)
			tsPacker[i] = read2();
		
		if (pid != 0x0100)
			return null;
		//Log.write("AUDIO found");
		return tsPacker;
	}
	
}