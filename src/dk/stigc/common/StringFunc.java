package dk.stigc.common;

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
	
	public static String replace(String s, String sub, String with)
    {
        int i = s.indexOf(sub, 0);
        if (i == -1)
            return s;
            
    	int c=0;
    	int tokenLength = sub.length();
        StringBuilder buf = new StringBuilder(s.length());
		
        do
        {
            buf.append(s.substring(c,i));
            buf.append(with);
            c=i+tokenLength;
            i=s.indexOf(sub,c);
        } while (i!=-1);
        
        if (c<s.length())
            buf.append(s.substring(c,s.length()));
        
        return buf.toString();
    }
	
	public static int indexOfIgnoreCase(String text, String search)
	{
		if (search.length()==0)
			return 0;
		
		if (text.length()==0)
			return -1;

		for (int i=0; i<text.length(); i++)
		{
			if (i + search.length() > text.length())
				return -1;
			
			boolean match = true;
			
			for (int j=0; j<search.length(); j++)
			{
				if (Character.toLowerCase(text.charAt(i+j)) 
						!= Character.toLowerCase(search.charAt(j)))
				{
					match = false;
					break;
				}
			}
			
			if (match)
				return i;
		}

		return -1;
	}
}
