package dk.stigc.javatunes.audioplayer.player;

import com.jcraft.jorbis.*;
import com.jcraft.jogg.*;

import dk.stigc.javatunes.audioplayer.other.*;

public class OggPlayer extends BasePlayer
{
  static final int BUFSIZE=4096*8;
  static int convsize=BUFSIZE*2;
  static byte[] convbuffer=new byte[convsize]; 

  SyncState oy;
  StreamState os;
  Page og;
  Packet op;
  Info vi;
  Comment vc;
  DspState vd;
  Block vb;

 
  //private double gain = 1;
  //private long timer = 0;
  //private boolean paused = false;
  //public boolean running=true;

  //Song song;
  byte[] buffer=null;
  int bytes=0;
  int counter=0;
  

	

  //int frameSizeInBytes;
  //int bufferLengthInBytes;

/*
  public OggPlayer(String fileName)
  {
  	this.fileName = fileName;
  	setPriority(MAX_PRIORITY);
  }
  */
  
  void init_jorbis()
  {
    oy=new SyncState();
    os=new StreamState();
    og=new Page();
    op=new Packet();
    vi=new Info();
    vc=new Comment();
    vd=new DspState();
    vb=new Block(vd);
    buffer=null;
    bytes=0;
    
    oy.init();
  }

/*
  void createOutputLine(int channels, int rate)
  {
    if(outputLine==null || this.rate!=rate || this.channels!=channels)
    {
      //Drain last....
      //if(outputLine!=null)
      //{
        //outputLine.drain();
        //outputLine.stop();
        //outputLine.close();
      //}

      //Log.write ("rate:" + rate);
      //Log.write ("channels:" + channels);
      Log.write ("New Vorbis Line");
      init_audio(channels, rate);
      outputLine.start();
    }
    else
    {
	    outputLine.stop();
	    outputLine.start();
	}
  }

  void init_audio(int channels, int rate)
  {
    try 
    {
      AudioFormat af = new AudioFormat((float)rate, 16, channels,true,false);
      DataLine.Info info = 	new DataLine.Info(SourceDataLine.class, af); //AudioSystem.NOT_SPECIFIED
      if (!AudioSystem.isLineSupported(info))
    	return;

      try
      {
      	int bufferSize = Settings.getPropertyAsInt("SoundBuffer", 0);
    	outputLine = (SourceDataLine) AudioSystem.getLine(info);
    	if (bufferSize>0)
    		outputLine.open(af, bufferSize);
    	else
    		outputLine.open(af);
    	Log.write("Vorbis Buffer: " + outputLine.getBufferSize());
    	
      } 
      catch (LineUnavailableException ex) { 
    System.out.println("init_audio " + ex);
        return;
      } 
      catch (IllegalArgumentException ex) { 
    System.out.println("init_audio: " + ex);
    return;
      }

      //frameSizeInBytes = audioFormat.getFrameSize();
      //int bufferLengthInFrames = outputLine.getBufferSize()/frameSizeInBytes/2;
      //bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

      //buffer = new byte[bufferLengthInBytes];
      //if(originalClassLoader!=null)
      //  Thread.currentThread().setContextClassLoader(originalClassLoader);

      this.rate=rate;
      this.channels=channels;
    }
    catch(Exception ee){
      System.out.println(ee);
    }
  }
*/

  public void decode() throws Exception
  	{
      init_jorbis();
  loop:
    while(true){
    
      int eos=0;

      int index=oy.buffer(BUFSIZE);
      buffer=oy.data;

      bytes=bin.read(buffer, index, BUFSIZE); 
      
      oy.wrote(bytes);

      if(oy.pageout(og)!=1){
        if(bytes<BUFSIZE)break;
        Log.write("Invalid Ogg bitstream.");
        return;
      }

      os.init(og.serialno());
      os.reset();

      vi.init();
      vc.init();

      if(os.pagein(og)<0){ 
        // error; stream version mismatch perhaps
        Log.write("Err reading first page.");
        return;
      }
	

      if(os.packetout(op)!=1){ 
        // no page? must not be vorbis
        Log.write("Err reading initial header packet.");
        break;
//      return;
      }

      if(vi.synthesis_headerin(vc, op)<0){ 
        // error case; not a vorbis header
        Log.write("No Vorbis data.");
        return;
      }
		
      int i=0;

      while(i<2){
        while(i<2){
      int result=oy.pageout(og);
      if(result==0) break; // Need more data
      if(result==1){
            os.pagein(og);
        while(i<2){
          result=os.packetout(op);
          if(result==0)break;
          if(result==-1){
            Log.write("Corrupt secondary header.");
                //return;
                break loop;
          }
          vi.synthesis_headerin(vc, op);
          i++;
        }
      }
    }

        index=oy.buffer(BUFSIZE);
        buffer=oy.data; 
        try
        { 
        	bytes=bin.read(buffer, index, BUFSIZE); 
        }
        catch(Exception e)
        {
          Log.write("b: " + e);
          return;
    	}
    	
        if(bytes==0 && i<2)
        {
      		Log.write("End of file!");
          	return;
    	}
    	
    oy.wrote(bytes);
      }

      convsize=BUFSIZE/vi.channels;

      vd.synthesis_init(vi);
      vb.init(vd);

      float[][][] _pcmf=new float[1][][];
      int[] _index=new int[vi.channels];

      synchronized (audioInfo)
      {
    	  if (audioInfo.granules > 0) 
    		  audioInfo.lengthInSeconds = audioInfo.granules / vi.rate;
    	  else if (audioInfo.contenLength > 0)
    		  audioInfo.lengthInSeconds = (int)(audioInfo.contenLength * 8 / vi.bitrate_nominal);
    	  
    	  setBitRateFromFileLength();
    	  audioInfo.kbps = vi.bitrate_nominal/1000;
      }
	
	//Log.write("3");
		//cb.sendMessage(null, "UpdateSongInfo");
      	//createOutputLine(vi.channels, vi.rate);
//      	Log.write ("
      	initAudioLine(vi.channels, vi.rate, 16, true, false);
      	
      	//setVolume(gain);
      	//timer = System.currentTimeMillis();
  		//Log.write ("available: " + outputLine.available());

      while(eos==0){
        while(eos==0){
		
		//System.out.print("O");
		/*
		if (paused)
		{
			try
			{
				sleep(100);
			}
			catch (Exception ex) {}
			continue;
		}*/


      int result=oy.pageout(og);
      if(result==0) break; // need more data
      if(result==-1){ // missing or corrupt data at this page position
//      Log.write("Corrupt or missing data in bitstream; continuing...");
      }
      else{
            os.pagein(og);
       
  		//if (outputLine!=null)
  		//Log.write ("4.: " + id + " : "  + outputLine.available());
  		        
        while(true){
          result=os.packetout(op);
          if(result==0)break; // need more data
          if(result==-1){ // missing or corrupt data at this page position
                     // no reason to complain; already complained above
          }
              else{
                // we have a packet.  Decode it
            int samples;
            if(vb.synthesis(op)==0){ // test for success!
          vd.synthesis_blockin(vb);
        }
            while((samples=vd.synthesis_pcmout(_pcmf, _index))>0){
              float[][] pcmf=_pcmf[0];
          int bout=(samples<convsize?samples:convsize);
        
          // convert doubles to 16 bit signed ints (host order) and
          // interleave
          for(i=0;i<vi.channels;i++){
            int ptr=i*2;
            //int ptr=i;
            int mono=_index[i];
            for(int j=0;j<bout;j++){
              int val=(int)(pcmf[i][mono+j]*32767.);
              if(val>32767){
                val=32767;
                //clipflag=true;
              }
              if(val<-32768){
                val=-32768;
                //clipflag=true;
              }
             
              if(val<0) val=val|0x8000;
              convbuffer[ptr]=(byte)(val);
              convbuffer[ptr+1]=(byte)(val>>>8);
              ptr+=2*(vi.channels);
            }
          }

  		  		//if (outputLine!=null)
  				//	Log.write ("5.: " + id + " : "  + outputLine.available());
  					
                  //writeData(convbuffer, 2*vi.channels*bout);
                //counter++;
                //if (counter%2==0)
				//out.write(convbuffer, 0, 2*vi.channels*bout);
				write(convbuffer, 2*vi.channels*bout);
				vd.synthesis_read(bout);
      			if (!running) 
      				return;          			
            }     
          }
        }
        if(og.eos()!=0)eos=1;
      }
        }

        if(eos==0){
      index=oy.buffer(BUFSIZE);
      buffer=oy.data;
      
      bytes = bin.read(buffer,index,BUFSIZE); 

      if(bytes==-1)
            break;
      
      oy.wrote(bytes);
      
      if(bytes==0)
    	  eos=1;
        }
      }

      os.clear();
      vb.clear();
      vd.clear();
      vi.clear();
    }

    oy.clear();
  }
}
