package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import javax.sound.sampled.LineUnavailableException;

import dk.stigc.javatunes.audioplayer.other.*;

abstract public class BasePlayer extends Thread
{	  	
	IAudioPlayerHook hook;
  	protected BufferedInputStream bin;
  	private static SourceDataLineManager dlm = new SourceDataLineManager();
  	public static boolean noOutput = false; //Used for decoding speed test
  	protected volatile boolean running = true;  
  	public AudioInfo audioInfo;
	private int speed = 1;
	private int bps, rate, channels;
	private long totalBytes;
	private static Object writerLock = new Object();
	private boolean bigEndian;
	
	private double startGain;
	private double rpgain = 1;
	private volatile static double globalRpgain;

	static 
	{
		setGlobalRpgain(0);
	}
	
	public static void setGlobalRpgain(double db)
	{
		Log.write("global rpgain set to " + db + " db");
		globalRpgain = Math.pow(10, db / 20.0);
	}
		
	public void setData(InputStream in, IAudio audio, AudioInfo audioInfo, double startGain, boolean albumMode)
	{
		setPriority(MAX_PRIORITY);
		this.audioInfo = audioInfo;
		this.startGain = startGain;		

		bin = new BufferedInputStream(in);
		
		if (true)
		{
			double db = audio.getReplayGain(albumMode);
			if (db!=0 && Math.abs(db)<50)
			{
				Log.write("rpgain is " + db + " db");
				rpgain = Math.pow(10, db / 20.0);
			}
		}
	}
	
	public void pause()
	{
		dlm.pause();
	}	

	public void setSpeed(int speed) 
	{
		this.speed = speed;
	}	

	public void setVolume(double gain) 
	{
		dlm.setVolume(gain);
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

	public void setBitRateFromFileLength()
	{
		synchronized (audioInfo)
		{
			if (audioInfo.contenLength>0 && audioInfo.lengthInSeconds > 0)
				audioInfo.kbps = (int) (audioInfo.contenLength / audioInfo.lengthInSeconds * 8/1000);
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
  	
	private void applyGain(byte[] data, int length)
  	{
  		double totalrpgain = rpgain * globalRpgain;
  		
  		if (totalrpgain==1)
  			return;
  		
  		if (bps==16)
  		{
  			if (!bigEndian)
  			{
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
  			else
  			{
		  		for (int i=0; i<length; i+=4)
				{
					int v1 = (data[i+3] & 0xff) | (data[i+2] << 8);
					int v2 = (data[i+1] & 0xff) | (data[i+0] << 8);
					v1 = applyGain(v1, totalrpgain);
					v2 = applyGain(v2, totalrpgain);
		            data[i+3] = (byte) (v1 & 0xff);
		            data[i+2] = (byte) ((v1 >> 8) & 0xff);
		            data[i+1] = (byte) (v2 & 0xff);
		            data[i+0] = (byte) ((v2 >> 8) & 0xff);
				}   				
  			}
	
  		}
		else if (bps==8)
		{
  			for (int i=0; i<length; i++)
  			{
  				int v = (data[i] & 0xff);
  				v *= totalrpgain;
   				data[i] = (byte) (v & 0xff);
  			}
		}
  	}
  	
	protected void write(byte[] data, int length)
  	{
  		if (!running || noOutput)
  			return;
  		
  		applyGain(data, length);
  		
  		totalBytes += length;
  		
  		//Log.write("this:" + this);
  		synchronized (writerLock)
  		{
			int orgLength = length;
			
			if (speed>1 && bps==16)
			{
				length = length/4;
				if (length%4>0)
					length += (4-(length%4));
					
				int index= 0;
				for (int i=0; i<orgLength; i+=16)
				{
					int v1 = (data[i+0] & 0xff) | (data[i+1] << 8);
					int v2 = (data[i+2] & 0xff) | (data[i+3] << 8);
					v1 = v1 / 4;
					v2 = v2 / 4;
	                data[index+0] = (byte) (v1 & 0xff);
	                data[index+1] = (byte) ((v1 >> 8) & 0xff);
	                data[index+2] = (byte) (v2 & 0xff);
	                data[index+3] = (byte) ((v2 >> 8) & 0xff);
	                index+=4;
				}
			}
			
	
	  		int r = 0;
	  		do
	  		{
	  			r += dlm.write(data, r, length-r);//, decoderId);	
				//lastSample1 = (data[r-4] & 0xff) | (data[r-3] << 8);
				//lastSample2 = (data[r-2] & 0xff) | (data[r-1] << 8);
	  		}
	  		while (r<length && running);
	  		//bytes += orgLength;
  		}
  		
  		calculatePosition();
  	}

	public void stop(boolean forced)
	{
		running = false;
		if (forced)
		{
			dlm.stopLine();
		}
	}
	
	public abstract void decode() throws Exception;
	
	public void run() 
	{
		boolean finished = false;
		try 
		{
			decode();
			finished = true;
		} 
		catch (Exception ex) 
		{
			hook.trackDecodingError(ex);
		}
		finally 
		{
			hook.trackEnded(finished);
			Common.close(bin);
		}
	}  
		

	
  	protected void initAudioLine(int channels, int rate, int bps, boolean signed, boolean bigEndian) throws LineUnavailableException
  	{
  		audioInfo.init(channels, rate, bps);

  		this.bps = bps;
  		this.rate = rate;
  		this.channels = channels;
  		this.bigEndian = bigEndian;
  		
  		dlm.initAudioLine(channels, rate, bps, signed, bigEndian, startGain);
	}
	
}
