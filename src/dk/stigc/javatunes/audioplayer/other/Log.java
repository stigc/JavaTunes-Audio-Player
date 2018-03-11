package dk.stigc.javatunes.audioplayer.other;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	static boolean enabled = true;
	static Logger logger;
	
	static
	{
		logger = Logger.getLogger("javatunes.mediaplayer");
	}
	
	public static void write(Exception ex)
	{
		write("", ex);
	}

	public static void write(String str)
	{
		if (enabled)
		{
			logger.info(str);
		}
	}

	public static void write(String msg, Exception ex)
	{
		if (enabled)
		{
			logger.log(Level.WARNING, msg, ex);
		}
	}
}
