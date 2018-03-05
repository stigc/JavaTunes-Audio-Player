package dk.stigc.javatunes.audioplayer.player;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javaFlacEncoder.*;
import javaFlacEncoder.EncodingConfiguration.SubframeType;

public class FlacEncoder
{
	FLACEncoder flacEncoder;
	FLACOutputStream flacOutputStream;
	
	public FlacEncoder(File file, OutputStream os) throws IOException
	{
		StreamConfiguration streamConfiguration = new StreamConfiguration();
		streamConfiguration.setSampleRate(44100);
		streamConfiguration.setBitsPerSample(16);
		streamConfiguration.setChannelCount(2); 
		
		if (file != null)
			flacOutputStream = new FLACFileOutputStream(file);
		else
			flacOutputStream = new FLACStreamOutputStream(os);
		
		flacEncoder = new FLACEncoder();
		flacEncoder.setStreamConfiguration(streamConfiguration);
		flacEncoder.setOutputStream(flacOutputStream);
		flacEncoder.openFLACStream();	
	}
	
	public void write(byte[] pcm, int length) throws IOException
	{
		int[] sampleData = new int[length / 2];

        for (int i=0; i<length; i+=2)
        {
        	ByteBuffer bb = ByteBuffer.wrap(pcm, i, 2); 
        	bb.order(ByteOrder.LITTLE_ENDIAN);
            sampleData[i/2] = bb.getShort();    	
        }
        
 	    flacEncoder.addSamples(sampleData, sampleData.length / 2);
		flacEncoder.encodeSamples(sampleData.length, false);
	}
	
	public void stop() throws IOException
	{
		flacEncoder.encodeSamples(flacEncoder.samplesAvailableToEncode(), true);
	}
}
