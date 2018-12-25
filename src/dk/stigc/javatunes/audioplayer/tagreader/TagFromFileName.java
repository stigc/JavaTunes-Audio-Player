package dk.stigc.javatunes.audioplayer.tagreader;

import dk.stigc.common.StringFunc3;

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
    
    	
	public static String[] getMetaDataFromFileName(String filePath) 
	{
		String str = trimToFirstLetter(filePath);
		
		if (StringFunc3.isNullOrEmpty(str))
			return new String[] {"", filePath};
		
		int index = str.indexOf("-");
		String arr[] = new String[2];
		
		if (index>0)
		{
			
			arr[0] = str.substring(0,index).replace('_', ' ').trim();
			arr[1]= str.substring(index+1).replace('_', ' ').trim();
			return arr;
		}
		else
		{
			arr[0] = "Unknown";
			arr[1] = str.replace('_', ' ').trim();
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


