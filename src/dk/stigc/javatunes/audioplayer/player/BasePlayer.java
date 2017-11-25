package dk.stigc.javatunes.audioplayer.player;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.LineUnavailableException;

import dk.stigc.javatunes.audioplayer.other.*;
import javaFlacEncoder.FLACEncoder;
import javaFlacEncoder.FLACFileOutputStream;
import javaFlacEncoder.StreamConfiguration;

abstract public class BasePlayer extends Thread
{	  	
	public IPlayBackAPI playBackApi;	
  	public AudioInfo audioInfo;	
	IAudioPlayerHook hook;
	
  	protected BufferedInputStream bin;
  	protected volatile boolean running = true;
  	protected volatile boolean ended = false; 

  	private IAudio audio;
	private int bps, rate, channels;
	private long totalBytes;
	private boolean bigEndian;
	private double startGain;
	private double tagReplayGain = 1;
	private volatile static double globalReplayGain = 1;
		
	public void initialize(InputStream in, IAudio audio, AudioInfo audioInfo
			, double startGain, boolean isAlbumMode)
	{
		setPriority(MAX_PRIORITY);
		this.audio = audio;
		this.audioInfo = audioInfo;
		this.startGain = startGain;		

		bin = new BufferedInputStream(in);
		
		if (true)
		{
			double db = audio.getReplayGain(isAlbumMode);
			if (db!=0 && Math.abs(db)<50)
			{
				Log.write("rpgain is " + db + " db");
				tagReplayGain = Math.pow(10, db / 20.0);
			}
		}
	}

	public static void setGlobalRpgain(double db)
	{
		Log.write("global rpgain set to " + db + " db");
		globalReplayGain = Math.pow(10, db / 20.0);
	}
	
	private void calculatePosition() 
	{
		synchronized (audioInfo)
		{
			int bytesPerSecond = channels * rate * (bps/8);
			double seconds = (double)totalBytes / bytesPerSecond;
			audioInfo.positionInMs =  (long)(seconds * 1000);
		}
	}

	public void trySetBitRateFromFileLength()
	{
		synchronized (audioInfo)
		{
			if (audioInfo.lengthInBytes>0 && audioInfo.lengthInSeconds > 0)
				audioInfo.kbps = (int) (audioInfo.lengthInBytes / audioInfo.lengthInSeconds * 8/1000);
		}
	}

  	private int applyGain(int v, double gain)
  	{
  		v *= gain;
  		//prevent clipping
		if (v>32767) 
			v = 32767;
		else if (v<-32768) 
			v = -32768;
		return v;
		  		
  	}
  	
  	private void ensureLittleEndian(byte[] pcm, int length)
  	{
  		if (!bigEndian)
  			return;
  		
  		if (bps==16)
  			for (int i=0; i<length; i+=2)
  			{
  				byte temp = pcm[i];
  				pcm[i] = pcm[i+1];
  				pcm[i+1] = temp;
  			}
  		else if (bps==24)
  			for (int i=0; i<length; i+=3)
  			{
  				byte temp = pcm[i];
  				pcm[i] = pcm[i+2];
  				pcm[i+2] = temp;
  			}
  	}
  	
	private void applyGain(byte[] data, int length)
  	{
  		double totalrpgain = tagReplayGain * globalReplayGain;
  		  		
  		if (totalrpgain==1 || bps != 16)
  			return;
  		
  		for (int i=0; i<length; i+=4)
		{
			int v1 = (data[i+0] & 0xff) | (data[i+1] << 8);
			int v2 = (data[i+2] & 0xff) | (data[i+3] << 8);
			v1 = applyGain(v1, totalrpgain);
			v2 = applyGain(v2, totalrpgain);
            data[i+0] = (byte) (v1 & 0xff);
            data[i+1] = (byte) ((v1 >> 8) & 0xff);
            data[i+2] = (byte) (v2 & 0xff);
            data[i+3] = (byte) ((v2 >> 8) & 0xff);
		} 
  	}
	
	protected void write(byte[] pcm, int length)
  	{
  		ensureLittleEndian(pcm, length);
  		
  		applyGain(pcm, length);

  		if (!running)
  			return;
  		
  		playBackApi.writeToFlacOutput(pcm, length, bps, rate, channels);

  		if (!running)
  			return;
  		
  		totalBytes += length;

  		int index = 0;
  		while (running)
  		{
  			index += playBackApi.write(pcm, index, length-index);
  			
  			if (index >= length)
  				break;
  		}
  		
  		calculatePosition();
  	}

	public void stopThread()
	{
		running = false;
	}
	
	public abstract void decode() throws Exception;
	
	public void run() 
	{
		Exception ex = null;
		
		try 
		{
			decode();
		} 
		catch (Exception ex2) 
		{
			ex = ex2;
		}
		finally 
		{
			ended = true;
			Common.close(bin);
		}
		
		if (ex != null)
		{
			Log.write("audioFailed", ex);
			if (hook != null)
				hook.audioFailed(audio, ex);
		}
		else if (running)
		{
			Log.write("audioEnded");
			if (hook != null)				
				hook.audioEnded(audio);
		}
		else
		{
			Log.write("audioInterrupted");
			if (hook != null)				
				hook.audioInterrupted(audio);
		}
	}  
		

	
  	protected void initAudioLine(int channels, int rate, int bps, boolean signed, boolean bigEndian) throws LineUnavailableException
  	{
  		audioInfo.init(channels, rate, bps);

  		this.bps = bps;
  		this.rate = rate;
  		this.channels = channels;
  		this.bigEndian = bigEndian;
  		
  		//dlm.initAudioLine(channels, rate, bps, signed, startGain);
  		playBackApi.initAudioLine(channels, rate, bps, signed, startGain);
	}
	
}
