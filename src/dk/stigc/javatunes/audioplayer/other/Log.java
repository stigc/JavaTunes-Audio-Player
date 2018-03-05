package dk.stigc.javatunes.audioplayer.other;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	static boolean enabled = true;
	static String logger = "javatunes.mediaplayer";
	
	public static void write(Exception ex)
	{
		write("", ex);
	}

	public static void write(String str)
	{
		if (enabled)
			Logger.getLogger(logger).info(str);
	}

	public static void write(String msg, Exception ex)
	{
		
		if (enabled)
		{
//			StringWriter sw = new StringWriter();
//			ex.printStackTrace(new PrintWriter(sw));
//			String exceptionDetails = sw.toString();
			  
			Logger.getLogger(logger).log(Level.WARNING, msg, ex);
		}
	}
}
