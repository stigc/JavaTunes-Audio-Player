package dk.stigc.javatunes.audioplayer.player;

import com.beatofthedrum.alacdecoder.*;

public class AlacPlayer extends BasePlayer
{
    public void decode() throws Exception
    {
		AlacContext ac = AlacUtils.AlacOpenFileInput(bin);
		int total_samples = AlacUtils.AlacGetNumSamples(ac);
		int sample_rate = AlacUtils.AlacGetSampleRate(ac);
        int num_channels = AlacUtils.AlacGetNumChannels(ac);
		int bitps = AlacUtils.AlacGetBitsPerSample(ac);
		audioInfo.lengthInSeconds = (total_samples / sample_rate);
		initAudioLine(num_channels, sample_rate, bitps, true, false);	
			    	
   		int destBufferSize = 1024 *24 * 3; // 24kb buffer = 4096 frames = 1 alac sample (we support max 24bps)
		byte[] pcmBuffer = new byte[65536];
		int total_unpacked_bytes = 0;
		int bytes_unpacked;
		
		int[] pDestBuffer = new int[destBufferSize]; 

		int bps = AlacUtils.AlacGetBytesPerSample(ac);
		
		
		while (true)
		{
			bytes_unpacked = 0;

			bytes_unpacked = AlacUtils.AlacUnpackSamples(ac, pDestBuffer);

			total_unpacked_bytes += bytes_unpacked;

			if (bytes_unpacked > 0)
			{
				pcmBuffer = format_samples(bps, pDestBuffer, bytes_unpacked);
				write(pcmBuffer, bytes_unpacked);
			}

			//Log.write("bytes_unpacked: " + bytes_unpacked);
			if (bytes_unpacked == 0)
				break;
			if (!running) 
				return;				
		}
    }
 	
 	public static byte[] format_samples(int bps, int[] src, int samcnt)
    {
        int temp = 0;
        int counter = 0;
        int counter2 = 0;
        byte[] dst = new byte[65536];

        switch (bps)
        {
            case 1:
                while (samcnt > 0)
                {
                    dst[counter] =  (byte)(0x00FF & (src[counter] + 128));
                    counter++;
                    samcnt--;
                }
				break;

			case 2:
				while (samcnt > 0)
				{
					temp = src[counter2];
					dst[counter] =  (byte)temp;
					counter++;
					dst[counter] =  (byte)(temp >>> 8);
					counter++;
					counter2++;
					samcnt = samcnt - 2;
                }
				break;

            case 3:
                while (samcnt > 0)
                {
                    dst[counter] =  (byte)src[counter2];
                    counter++;
                    counter2++;
                    samcnt--;
                }
				break;
        }

        return dst;
    }

}