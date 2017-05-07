package dk.stigc.javatunes.audioplayer.other;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Log
{
	static boolean enabled = false;
	static String logger = "javatunes.mediaplayer";
	
	public static void write(Exception ex)
	{
		if (enabled)
			Logger.getLogger(logger).log(Level.WARNING, "", ex);
	}

	public static void write(String str)
	{
		if (enabled)
			Logger.getLogger(logger).info(str);
	}

	public static void write(String msg, Exception ex)
	{
		if (enabled)
			Logger.getLogger(logger).log(Level.WARNING, msg, ex);
	}
}
