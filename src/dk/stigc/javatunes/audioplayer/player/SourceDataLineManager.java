package dk.stigc.javatunes.audioplayer.player;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.*;
import javax.sound.sampled.AudioFormat.Encoding;

import dk.stigc.javatunes.audioplayer.other.*;

class SourceDataLineManager implements IPlayBackAPI
{
	private boolean outputToMixer = true;
	private AudioFormat audioFormat;
	private boolean paused;
	private SourceDataLine out;
	private int bufferSizeInKb;
	private FlacEncoder flacEncoder;
	
	public boolean flacOutputIsEnabled()
	{
		return flacEncoder != null;
	}
	
	public synchronized void enableFlacOutput(File file, OutputStream os) throws IOException
	{
		if (flacEncoder != null)
			throw new RuntimeException("Cannot start new output before last is closed");
		
		flacEncoder = new FlacEncoder(file, os);
	}
	
	public synchronized void setOutputToMixer(boolean value)
	{
		outputToMixer = value;
	}
	
	public synchronized void finishFlacOutput() throws IOException
	{
		FlacEncoder temp = flacEncoder;
		
		flacEncoder = null;
		
		temp.stop();
	}
	
	public synchronized void discardDataInLine()
	{
		if (out!=null)
		{
			//discards data...
			out.flush();
		}
	}
	
	public synchronized void pause()
	{
		paused = true;
		
		if (out != null)
			out.stop();
	}
	
	public synchronized void start()
	{
		paused = false;
		
		if (out != null)
			out.start();
		
		this.notify();
	}

	public synchronized void setBufferSize(int sizeInKb)
	{
		bufferSizeInKb = sizeInKb;
	}

	public synchronized boolean isPaused()
	{
		return paused;
	}
	
	public synchronized void waitIfPaused()
	{
		while (paused)
		{
			try
			{
				Log.write("Waiting, is paused");
				this.wait();
			} 
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}
	}
	
	public synchronized int write(byte[] data, int start, int length)
  	{	
		if (!outputToMixer)
			return length;

		return out.write(data, start, length);
  	}
	
	public synchronized void setVolume(double gain) 
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
	
	public synchronized void initAudioLine(int channels, int rate, int bps, boolean signed, double gain) throws LineUnavailableException
	{
  		//reuse line?
  		if (out!=null 
  			&& audioFormat.getSampleRate()==rate 
  			&& audioFormat.getChannels()==channels
  			&& audioFormat.getSampleSizeInBits()==bps
  			&& (audioFormat.getEncoding()==Encoding.PCM_SIGNED) == signed)
  			return;
  		
  		audioFormat= new AudioFormat((float)rate, bps, channels, signed, false);
  		DataLine.Info info = new DataLine.Info(SourceDataLine.class, audioFormat);
  		out = (SourceDataLine)AudioSystem.getLine(info);
		
  		Log.write("AudioFormat set to " + audioFormat);
  				
		if (bufferSizeInKb>0)
		{
			Log.write("Using explicit buffer size " + bufferSizeInKb + " KB");
			out.open(audioFormat, bufferSizeInKb*1024);
		}
		else
		{
			out.open(audioFormat);
		}
		
		setVolume(gain);

		//if someone has pressed pause before decoder has called initAudioLine
		if (!paused)
			out.start();
	}

	@Override
	public synchronized void writeToFlacOutput(byte[] pcm, int length, int bps, int rate, int channels)
	{
		if (flacEncoder == null)
			return;
		
		//supported format 16bps 22500/41000 hz and 1/2 channels
		if (bps != 16 || (rate != 44100 && rate != 22050) || (channels != 1 && channels != 2))
			return;

		if (channels == 1)
		{
			//Log.write("Converting to stereo");
			byte[] temp = new byte[length*2];
			for (int i=0; i<length; i+=2)
			{
				temp[i*2] = pcm[i];
				temp[i*2+1] = pcm[i+1];
				temp[i*2+2] = pcm[i];
				temp[i*2+3] = pcm[i+1];
			}
			pcm = temp;
			length *= 2;
		}
		
		if (rate == 22050)
		{
			//Log.write("Converting to 44.100");
			byte[] temp = new byte[pcm.length*2];
			for (int i=0; i<length; i+=4)
			{
				temp[i*2+0] = pcm[i];
				temp[i*2+1] = pcm[i+1];
				temp[i*2+2] = pcm[i+2];
				temp[i*2+3] = pcm[i+3];
				temp[i*2+4] = pcm[i];
				temp[i*2+5] = pcm[i+1];
				temp[i*2+6] = pcm[i+2];
				temp[i*2+7] = pcm[i+3];				
			}
			pcm = temp;
			length *= 2;
		}
		
		try
		{
			flacEncoder.write(pcm, length);
		} 
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}
}
