package dk.stigc.javatunes.audioplayer.player;

import javazoom.jl.decoder.*;

public class MP3Player extends BasePlayer
{	  
	private Bitstream bitstream;
	private Decoder	decoder; 
	private boolean isVbr;
	private byte[] byteBuf = new byte[4608];
	private boolean firstHeader = true;
	
	public void decode() throws Exception
	{
		bitstream = new Bitstream(bin);      	        
	    decoder = new Decoder();

	    boolean ret = true;
		
		while (ret)
		{
			ret = decodeFrame();
		}
	}

	
	protected boolean decodeFrame() throws Exception
	{		
   		Header h = bitstream.readFrame();

		if (firstHeader) 
		{
			if (h==null)
				throw new Exception("Missing mp3 header");
			extractBitrateAndPlayLength(h);
		}
		
		if (h==null || !running) return false;
		
		if (isVbr)
		{
			int bitrateNow = h.bitrate_instant();
			audioInfo.addVariableBitrate(bitrateNow);
		}
		
		SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitstream);
		
		if (firstHeader)
			initAudioLine(decoder.getOutputChannels(), decoder.getOutputFrequency(), 16, true, false);	
		
		firstHeader = false;
		
		int len = output.getBufferLength();
		byte[] b = toByteArray(output.getBuffer(), 0, len);
		write(b, len*2);	
																		
		bitstream.closeFrame();
  		return true;
	}
	
    private void extractBitrateAndPlayLength(Header h) throws Exception
    {
    	isVbr = h.vbr();
		int lengtInSeconds = (int)(h.total_ms((int)lengthInBytes)/1000);
		int kbps = h.bitrate()/1000;
		audioInfo.setKbps(kbps);
		
		//Hmm no length in header. Calculate from filesize and CBR header.
		if (lengtInSeconds==0 && lengthInBytes>0 && kbps>0)
			lengtInSeconds = (int)lengthInBytes/((kbps/8)*1000);
		
		audioInfo.setLengthInSeconds(lengtInSeconds);
    } 
    
	private byte[] getByteArray(int length)
	{
		if (byteBuf.length < length)
			byteBuf = new byte[length+1024];
		return byteBuf;
	}
		
	private byte[] toByteArray(short[] samples, int offs, int len)
	{
		byte[] b = getByteArray(len*2);
		int idx = 0;
		short s;
		while (len-- > 0)
		{
			s = samples[offs++];
			b[idx++] = (byte)s;
			b[idx++] = (byte)(s>>>8);
		}
		return b;
	}
}
