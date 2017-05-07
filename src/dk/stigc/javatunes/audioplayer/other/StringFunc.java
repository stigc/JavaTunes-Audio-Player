package dk.stigc.javatunes.audioplayer.other;

public class StringFunc 
{
	public static boolean isNullOrEmpty(String value)
	{
		return value==null || value.length()==0;
	}

	public final static boolean endsWithIgnoreCase(String s, String token)
	{
		if (s==null || token==null)
			return false;
			
		int l = token.length();
		if (s.length()<l)
			return false;
			
		for (int i=0; i<l; i++)
		{
			int searchStringIndex = s.length()-l+i;
			if (Character.toLowerCase(s.charAt(searchStringIndex))
				!=Character.toLowerCase(token.charAt(i)))
				return false;
		}
		return true;
	}
	
	public final static boolean startsWithIgnoreCase(String s, String token)
	{
		if (s==null || token==null)
			return false;
			
		int l = token.length();
		if (s.length()<l)
			return false;
			
		for (int i=0; i<l; i++)
		{
			if (Character.toLowerCase(s.charAt(i))
				!=Character.toLowerCase(token.charAt(i)))
				return false;
		}
		return true;
	}
}
