package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import javax.sound.sampled.LineUnavailableException;

import org.kc7bfi.jflac.*;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import dk.stigc.javatunes.audioplayer.other.*;

public class FLACPlayer extends BasePlayer implements PCMProcessor 
{
	Exception ex;
	
    private void calculatePlayLength(StreamInfo si)
    {
    	int samplerate = si.getSampleRate();
    	long samples = si.getTotalSamples();
    	synchronized(audioInfo)
    	{
    		audioInfo.lengthInSeconds = (int)(samples/samplerate);
    	}
    }
	
    public void decode() throws Exception
    {
		FLACDecoder decoder = new FLACDecoder(bin);
		decoder.addPCMProcessor(this);
		decoder.readMetadata();
		calculatePlayLength(decoder.getStreamInfo());
		setBitRateFromFileLength();
		
		while (true)
		{
			if (!running) 
				return;
			
			if (ex != null)
				throw ex;
			
			decoder.findFrameSync();
								
	        try
	        {
	            decoder.readFrame();
	            decoder.callPCMProcessors(decoder.frame);
	        } 
			catch (EOFException e) 
			{
				break; 
			} 	        
	        catch (FrameDecodeException e) 
	        {
	            Log.write("Bad frame: " + e);
	        }
		}
    }
    
    private int bitsPerSample;
 	public void processStreamInfo(StreamInfo si)
 	{
		//	if (si.getBitsPerSample()!=16)
		//		throw new RuntimeException("Bits depth not supported:" + si.getBitsPerSample());
		//Log.write("getBitsPerSample : " + si.getBitsPerSample());
		//Log.write("si.getChannels() : " + si.getChannels());
		//Log.write("si.getSampleRate() : " + si.getSampleRate());
		this.bitsPerSample = si.getBitsPerSample();
		int bitsPerSample = this.bitsPerSample==24 ? 16 : this.bitsPerSample;
		try
		{
			initAudioLine(si.getChannels(), si.getSampleRate(), bitsPerSample, true, false);
		} 
		catch (Exception ex)
		{
			this.ex = ex;
		}
    }
    
    public int from24to16(byte[] data, int length)
    {
        int index = 0;
        for (int i=0; i<length; i+=3)
        {
        	int b24 = (data[i] & 0xff) | ((data[i+1] & 0xff) << 8) | ((data[i+2] & 0xff) << 16);
        	int b16 = (int)(((double)b24)/256.0);
        	data[index] = (byte)(b16 & 0xff);
        	data[index+1] = (byte)((b16>>8) & 0xff);
        	index+=2;
        }
        return index;
    }
    
    public void processPCM(ByteData pcm) 
    {
    	//Log.write("*");
    	byte[] data = pcm.getData();
    	int length = pcm.getLen();
    	if (bitsPerSample==24)
    	{
    		
    		length = from24to16(data, length);
    	}
    		
        write(data, length);
    }
}