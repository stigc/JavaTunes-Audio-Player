package dk.stigc.javatunes.audioplayer.tagreader;

public class NoTagReader extends TagBase 
{
	@Override
	void parseImp(FileBuffer fb, byte[] external) throws Exception 
	{
		tagFound = false;
	}
}
