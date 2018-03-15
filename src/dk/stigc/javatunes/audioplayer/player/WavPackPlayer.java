package dk.stigc.javatunes.audioplayer.player;

import java.io.*;

import javax.sound.sampled.LineUnavailableException;

import wavpack.*;

public class WavPackPlayer extends BasePlayer
{
	static int SAMPLE_BUFFER_SIZE = 256;
    static int [] temp_buffer = new int[SAMPLE_BUFFER_SIZE];
    static byte [] pcm_buffer = new byte[4 * SAMPLE_BUFFER_SIZE];
    
    public void decode() throws LineUnavailableException
    {
    	DataInputStream dis = new DataInputStream(bin);
    	
    	WavpackContext wpc = WavPackUtils.WavpackOpenFileInput(dis);
		int channels = WavPackUtils.WavpackGetReducedChannels(wpc);
		long samples = WavPackUtils.WavpackGetNumSamples(wpc);
		int bps = WavPackUtils.WavpackGetBytesPerSample(wpc);
		long samplerate =  WavPackUtils.WavpackGetSampleRate(wpc);
		int bitsps = WavPackUtils.WavpackGetBitsPerSample(wpc);
		int mode = WavPackUtils.WavpackGetMode(wpc);
		
		//Log.write("bitsps: "  + bitsps);
		//Log.write("num_channels: "  + channels);
		//Log.write("total_samples: "  + samples);
		//Log.write("bps: "  + bps);
		//Log.write("WavpackGetSampleRate: "  + samplerate);
		//Log.write("WavpackGetMode: "  + mode);
		
		audioInfo.setLengthInSeconds((int)(samples/samplerate));
		
		boolean signed = true;
		int tmp = bitsps;
		if (tmp>16) tmp = 16;
		if (tmp==12) tmp = 16;
		if (tmp==8) signed = false;
		initAudioLine((int)channels, (int)samplerate, tmp, signed, false);
		
		while (true)
        {
			if (!running) 
				return;
				            	
            long samples_unpacked; // was uint32_t in C

            samples_unpacked = WavPackUtils.WavpackUnpackSamples(wpc, temp_buffer, SAMPLE_BUFFER_SIZE / channels);
			if (samples_unpacked > 0)
            {
                samples_unpacked = samples_unpacked * channels;
                pcm_buffer = format_samples(bps, temp_buffer, samples_unpacked);
                
                tmp = bps;
                if (tmp>2) tmp = 2;
                int length = (int)samples_unpacked * tmp;
				
				/*
				//24bit->16bit
                if (bps==3 || bps==4)
                {
                	int index = 0;
                	for (int i=0; i<samples_unpacked*bps; i+=bps)
                	{
                		long v = 
                		pcm_buffer[index] = pcm_buffer[i+1];
                		pcm_buffer[index+1] = pcm_buffer[i+2];
                		index+=2;
                	}
                	length = index;
                }
                */
                write(pcm_buffer, length);
            }

            if (samples_unpacked == 0)
                break;
        } // end of while
    }

	static byte [] format_samples(int bps, int src [], long samcnt)
    {
        int temp;
        int counter = 0;
        int counter2 = 0;
        byte [] dst = new byte[4 * SAMPLE_BUFFER_SIZE];
		double d;
		
        switch (bps)
        {
            case 1:
                while (samcnt > 0)
                {
                    dst[counter] = (byte) (0x00FF & (src[counter] + 128));
                    counter++;
                    samcnt--;
                }
                break;

            case 2:
                while (samcnt > 0)
                {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
			 case 3:
			 	d = 65536 / 16777216.0;
                while (samcnt > 0)
                {
                	
                    temp = src[counter2];
                    //Log.write("temp:" + temp);
                    temp = (int)(d*temp);
                    //Log.write("temp:" + temp);
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
			 case 4:
			 	d = 65536 / 4294967296.0;
                while (samcnt > 0)
                {
                	
                    temp = src[counter2];
                    //Log.write("temp:" + temp);
                    temp = (int)(d*temp);
                    //Log.write("temp:" + temp);
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;			
			/*
            case 3:
                while (samcnt > 0)
                {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    dst[counter] = (byte) (temp >>> 16);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
			*/
			/*
            case 4:
                while (samcnt > 0)
                {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    dst[counter] = (byte) (temp >>> 16);
                    counter++;
                    dst[counter] = (byte) (temp >>> 24);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
			*/
        }

        return dst;
    }	
}