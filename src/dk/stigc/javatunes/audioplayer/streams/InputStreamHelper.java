package dk.stigc.javatunes.audioplayer.streams;

import java.net.*;
import java.util.*; 
import java.io.*;

import dk.stigc.javatunes.audioplayer.other.*;

public class InputStreamHelper
{
	public int timeout = 5;
	public int contentLength;
	public int icyMetaInt;
	public String icyName, icyGenre;
	public String redirectLocation;
	public int httpResponseCode;
	public String programName = "JavaTunes";
	public InputStreamHelper ()	{}

	public InputStreamHelper (int timeout)
	{
		this.timeout = timeout;
	}
	
	private URLConnection createURLConnection(String location) throws Exception
	{
		location = location.replace('\\','/');
		location = location.replaceAll(" ","%20");
				
		Log.write("loading: " + location);
		URL url = new URL(location);
		URLConnection urlc = url.openConnection();	
		urlc.setConnectTimeout(timeout*1000);
		urlc.setReadTimeout(10000);
	    return urlc;	
	}
	
	//Lyrics + Last.fm Posting...
	public InputStream postToRemoteStream(String location, String post) throws Exception
	{
		URLConnection urlc = createURLConnection(location);
    	urlc.setDoInput(true);
        urlc.setDoOutput(true);
        DataOutputStream output = new DataOutputStream(urlc.getOutputStream());
        output.writeBytes(post);
        output.flush();
        output.close();
        InputStream is = urlc.getInputStream();
		return is;       
	}	
	
	public InputStream getRemoteInputStreamAsBrowser(String location) throws Exception
	{
		URLConnection urlc = createURLConnection(location);
		urlc.setRequestProperty("Accept", "*/*");
		urlc.setRequestProperty("Accept-Language", "en-us");
		urlc.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
		urlc.setRequestProperty("Connection", "Keep-Alive");			
		return getRemoteInputStreamImpl(urlc);
	}
	
	private void parseHeader(String header)
	{
	    //parse header
	    for (String line : header.split("\r\n"))
	    {
	    	String lc = line.toLowerCase();
	    	if (lc.startsWith("content-length:"))
	    		contentLength = Integer.parseInt(line.substring(16).trim());
	    	if (lc.startsWith("icy-metaint:"))
	    		icyMetaInt = Integer.parseInt(line.substring(12).trim());
	    	if (lc.startsWith("icy-name:"))
	    	{
	    		icyName = line.substring(9).trim();
	    		if (icyName.equals("127.0.0.1"))
	    			icyName = null;
	    	}
	    	if (lc.startsWith("icy-genre:"))
	    		icyGenre = line.substring(10).trim();
	    	if (lc.startsWith("location:"))
	    		redirectLocation = line.substring(9).trim();
	    	if (lc.startsWith("http/"))
	    		httpResponseCode = Integer.parseInt(lc.split(" ")[1]);
	    		
	    }
	    
	    Log.write("content-length=" + contentLength + ", icy-metaint=" + icyMetaInt + ", icy-name:" + icyName);
	}
	
	public InputStream httpGetWithIcyMetadata(String location) throws Exception
	{
		URL url = new URL(location);
		int port = url.getPort() < 0 ? 80 : url.getPort();
		StringBuilder b = new StringBuilder();
		b.append("GET ");
		//b.append(location);

		if (url.getPath().length() == 0) {
			b.append('/');
		} else {
			b.append(url.getPath());
			//b.append(location); //Work with DR.DK AAC Streams
		}
		
		b.append(" HTTP/1.1\r\n");
		
		//http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.23
		String portAsString = port == 80 ? "" : ":80";
		b.append("Host: " + url.getHost() + portAsString + "\r\n");
		b.append("User-Agent: " +  programName + "\r\n");
		b.append("Accept: */*\r\n");
		b.append("Icy-MetaData:1\r\n");
		b.append("\r\n");
		final String request = b.toString();
		Log.write("Sending to " + url);
		byte[] reqByte = request.getBytes("UTF-8");
		
		// open communication socket
		Socket socket = new Socket(url.getHost(), port);
		socket.setSoTimeout(1000*10);
		
		// feed the request
		OutputStream out = socket.getOutputStream();
		out.write(reqByte);

		//read header
		InputStream is =  socket.getInputStream();
		StringBuilder sb = new StringBuilder();
	    for (;;)
	    {
	    	if (sb.length()>4 && 
	    			sb.substring(sb.length()-4, sb.length()).equals("\r\n\r\n"))
	    		break;
	    	char c = (char)is.read();
	    	sb.append(c);
	    }
	    
	    
	    parseHeader(sb.toString());
	    
	    if (httpResponseCode == 404)
	    	throw new Response404Exception();
	    
	    if (httpResponseCode == 302)
	    {
	    	Log.write("HTTP 302 redirect: " + redirectLocation);
	    	return httpGetWithIcyMetadata(redirectLocation);
	    }
	    
	    
	    //contentLength = 1024*100;
	    	
	    if (contentLength>0)
	    {
			is = new ContentLengthAwareInputStream(is, contentLength);
	    }
	    
		return is;
	}
	

	
	public InputStream getRemoteInputStream(String location, String range) throws Exception
	{
		URLConnection urlc = createURLConnection(location);
		urlc.setRequestProperty("Icy-MetaData", "1");
		urlc.setRequestProperty("Accept", "*/*"); 
		urlc.setRequestProperty("User-Agent", programName); 
		if (range!=null)
			urlc.setRequestProperty("Range", range);
		return getRemoteInputStreamImpl(urlc);
	}
	
	private InputStream getRemoteInputStreamImpl(URLConnection urlc) throws Exception
	{
		InputStream is = null;
	
		//2 FORSØG!
		for (int i=0; i<2; i++)
		{					    			
			if (i>0)
			{
				Common.sleep(200);
				Log.write ("Try Again");
			}
    		
    		try
    		{
    			urlc.connect();
    			
				if (urlc instanceof HttpURLConnection)
				{
					int httpResponseCode = ((HttpURLConnection)urlc).getResponseCode();
					if (httpResponseCode == 404)
						throw new FileNotFoundException("404");
					if (httpResponseCode!=200)
						Log.write ("http reponse: " + httpResponseCode);
				}
				
				parseIcyMetaData(urlc);
				contentLength = urlc.getContentLength();
				//Log.write("contentLength: " + contentLength);
				is = urlc.getInputStream();
				break;		    			
    		}
    		catch(Exception ex)
    		{
    			if (i > 0)
    				throw ex;    			
    		}
		}

		return is;
	}	

	private void parseIcyMetaData(URLConnection urlc)
	{
		for (int i=0; ; i++) 
		{
			String name = urlc.getHeaderFieldKey(i);
			String value = urlc.getHeaderField(i);
		  
			if (name == null && value == null)
				break;
			
			//Log.write(name + "=" + value);
		  
			if (name!= null && value != null && value.trim().length()>0)
			{
			   if (name.equals("icy-metaint"))
				  icyMetaInt = Integer.parseInt(value.trim());
			   else if (name.equals("icy-name") && !value.equals("127.0.0.1"))
				   icyName = value.trim();
			   else if (name.equals("icy-genre"))
				   icyGenre = value.trim();
			}
		}	
		
		if (icyMetaInt>0)
			Log.write("icyMetaInt: " + icyMetaInt);
	}
	
	public String[] readLines(InputStream in, String encoding) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(in, encoding));
 		ArrayList<String> lines = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            lines.add(line);
        }
        
        Common.close(br);
        return (String[])lines.toArray(new String[]{});	
	}

	//Fills a array from a BufferedInputStream
	public static void write(InputStream is, String location) throws Exception
	{
		int read = 0;
		FileOutputStream fos = new FileOutputStream(location);
		while ((read = is.read()) != -1)
			fos.write(read);
		fos.close();
	}
	//Fills a array from a BufferedInputStream
	public static int readToArray(InputStream bis, byte[] data)
	{
		int index = 0;
		int read = 0;
		int size = data.length;
		try
		{ 			
			do
			{
				read = bis.read(data, index, size-index);
				//Log.write("read " + read);
				if (read>0) index += read;
			}
			while 
			(index<size && read!=-1);
		}
		catch(Exception ex)
        {
        	Log.write("Ex: " + ex);
        }
        
		return index;
        //if (index<size)
        //	Log.write("[ERROR reading full buffer : " + index + "]"); 
	}		

	//Fills a array from a BufferedInputStream
	private byte[] readBytes(FileInputStream in, int length)
	{
		byte[] data = new byte[length];
		try
		{ 
		    int index = 0;  
		    while (index < length) 
		    {  
		        int count = in.read(data, index, length-index);  
		        index += count;  
		    }  
		}
		catch(Exception ex)
        {
        	Log.write("readBytes", ex);
        	return null;
        	
        }
        return data;	
	}
		
	//Fills a array from a BufferedInputStream
	public byte[] readBytes(InputStream in)
	{
		byte[] data = null;
		
		try
		{ 
			ByteArrayOutputStream out = new ByteArrayOutputStream(32000);
			byte[] buf = new byte[8000];
			int n=0;		
			while ((n = in.read(buf)) > 0)
				out.write(buf, 0, n);
			data = out.toByteArray();
		}
		catch(Exception ex)
        {
        	Log.write("readBytes(2)", ex);
        }
        finally
        {
        	Common.close(in);
        }	
        
        return data;	
	}
		
	public byte[] readAsBytes(File file) 
	{
		try
		{
			FileInputStream is = new FileInputStream(file);
			return readBytes(is, (int)file.length());
		}
		catch (FileNotFoundException ex) {}
		return null;		
	}
	
	public String readAsString(InputStream in, String encoding)
	{
		try
		{
			byte[] bytes = readBytes(in);
			return new String(bytes, encoding);
		}
		catch (Exception ex) {}
		return null;		
	}
	
	public String readAsString(File file, String encoding)
	{
		try
		{
			byte[] bytes = readAsBytes(file);
			return new String(bytes, encoding);
		}
		catch (Exception ex) {}
		return null;
	}
}

