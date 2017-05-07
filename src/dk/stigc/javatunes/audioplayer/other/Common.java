package dk.stigc.javatunes.audioplayer.other;

import java.io.*;

public class Common
{
	public static void sleep(int milliseconds)
    {
		try {Thread.sleep(milliseconds);} 
		catch (Exception ex) {}    	
    }

	public static void close(BufferedReader br)
	{
    	try	{ br.close(); } catch (Exception ex) {}
	}	
	
	public static void close(InputStream is)
	{
    	try	{ is.close(); } catch (Exception ex) {}
	}	

	public static void close(OutputStream is)
	{
    	try	{ is.close(); } catch (Exception ex) {}
	}	
	
    public static boolean fileExists(String v) 
    {	
    	File f = new File(v);
    	return f.exists();
    } 

	public static byte[] resize(byte[] bytes, int newSize, boolean cutInFront)
	{
		byte[] newBytes = new byte[newSize];
		if (newSize>bytes.length)
			newSize = bytes.length;
		if (cutInFront)
		{
			int diff = bytes.length-newSize;
			//arraycopy(Object src, int srcPos, Object dest, int destPos, int length) 
			System.arraycopy(bytes, diff, newBytes, 0, newSize);	
		}
		else
			System.arraycopy(bytes, 0, newBytes, 0, newSize);	
			
		return newBytes;
	}
}
