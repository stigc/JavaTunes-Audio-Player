package dk.stigc.javatunes.audioplayer.tagreader;

public class TagFromFileName extends TagBase
{ 
	public String path;
	final static String fileSeparator = System.getProperty("file.separator");
	
  	protected void parseImp(FileBuffer fb, byte[] external) throws Exception
	{
		String arr[] = getMetaDataFromFileName(getFileName(path));
		if (arr[0].length()>0)
			addArtist(arr[0]);
		title = arr[1];
	}

    public static String getFileNameFromRemote(String v)
    {
    	while (v.indexOf("/")!=-1)
    		v = v.substring(v.indexOf("/")+1, v.length());
    	
    	if (v.lastIndexOf('.')>0)
    		v = v.substring(0, v.lastIndexOf('.'));
    		
    	return v;
    }

    public static String getFileName(String v)
    {
    	while (v.indexOf(fileSeparator)!=-1)
    		v = v.substring(v.indexOf(fileSeparator)+1, v.length());
    	
    	if (v.lastIndexOf('.')>0)
    		v = v.substring(0, v.lastIndexOf('.'));
    		
    	return v;
    }
    
    	
	public static String[] getMetaDataFromFileName(String v) 
	{
		v = trimToFirstLetter(v);
		int index = v.indexOf("-");
		String arr[] = new String[2];
		
		if (index>0)
		{
			
			arr[0] = v.substring(0,index).replace('_', ' ').trim();
			arr[1]= v.substring(index+1).replace('_', ' ').trim();
			return arr;
		}
		else
		{
			arr[0] = "Unknown";
			arr[1] = v.replace('_', ' ').trim();
		}
		
		return arr;
	}
	
	private static String trimToFirstLetter(String v)
	{
		int index = 0;
		while(index<v.length() && !Character.isLetter(v.charAt(index)))
			index ++;
		
		if (index>0)
			v = v.substring(index);
		
		return v;
	}
}


