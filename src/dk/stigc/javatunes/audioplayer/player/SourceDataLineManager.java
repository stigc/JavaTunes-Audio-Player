package dk.stigc.javatunes.audioplayer.player;

import javax.sound.sampled.*;

import dk.stigc.javatunes.audioplayer.other.*;

class SourceDataLineManager
{
	private volatile boolean paused;
	public volatile SourceDataLine out;
	private int bps, channels, rate;                           
	private boolean bigEndian, signed;
	private int bufferSizeInKb;
	
	public void stopLine()
	{
		if (out!=null)
		{
			out.flush();
		}
	}
	
	public synchronized void pause()
	{
		paused =! paused;

		if (out == null)
			return;

		if (paused)
		{
			out.stop();
		}
		else
		{
			out.start();
		}		
	}
	
	public int write(byte[] data, int start, int length)
  	{	
		return out.write(data, start, length);
  	}
	
	public void setVolume(double gain) 
	{
		if (out!=null)
		{
			float dB = (float)(Math.log(gain)/Math.log(10.0)*20.0);
			FloatControl gainControl = (FloatControl)out.getControl(FloatControl.Type.MASTER_GAIN);
		    float max = gainControl.getMaximum();
		    float min = gainControl.getMinimum();
		    if (dB>max) dB = max;
		    if (dB<min) dB = min;
			//Log.write("Volume is " + dB + " db");
			gainControl.setValue(dB);
		}
	}	

	public synchronized void initAudioLine(int channels, int rate, int bps, boolean signed, boolean bigEndian, double gain)
	{
  		//reuse line?
  		if (out!=null 
  			&& this.rate==rate 
  			&& this.channels==channels
  			&& this.bps==bps
  			&& this.signed==signed
			&& this.bigEndian==bigEndian)
  		{
  			if (paused) pause();
  			return;
  		}
		
		this.rate = rate;
  		this.channels = channels;
  		this.bps = bps;
  		this.signed = signed;
  		this.bigEndian = bigEndian;
  		  		
    	try 
    	{	
			AudioFormat af = new AudioFormat((float)rate, bps, channels, signed, bigEndian);
      		DataLine.Info info = new DataLine.Info(SourceDataLine.class, af);
      		out = (SourceDataLine)AudioSystem.getLine(info);
    		
    		if (bufferSizeInKb>0)
    			out.open(af, bufferSizeInKb*1024);
    		else
    			out.open(af);
	   		out.start();
	   		
	   		setVolume(gain);
	   		Log.write(af.toString() + ". BufferSize: " + out.getBufferSize());
      	} 
		catch (IllegalArgumentException ex) 
		{   
			String msg = "Unsupported audio format: " + rate + " / " + bps;
			Log.write(msg);
		}		
		catch (Exception ex) 
		{ 
			Log.write ("Init audio: " + ex);
		} 			
	}	
}
