package dk.stigc.javatunes.audioplayer.other;

import java.util.*; 

public class StringList extends ArrayList<String>
{
	private static final long serialVersionUID = 4177778480332950836L;

	public StringList()
  	{
  		super(1);
  	}

	public StringList(String s) 
  	{
		super(1);
		this.add(s);
  	}
	
	public StringList(StringList list)
  	{
  		for (String s: list)
  			this.add(s);
  	}

	public boolean add(String v)
  	{
  		return super.add(v);
  	}

  	public String toString(String seperator)
  	{
  		if (this.size()==0)
  			return "";
  		
  		if (this.size()==1)
  			return this.get(0);
  		
  		String r = "";
  		for (String s: this)
  		{
  			if (r.length()>0)
  				r += seperator;
  			r += s;
  		}
  			
  		return r;
  	}	
	
	public boolean isTheSame(StringList list)
	{
		if (this.size()!=list.size())
			return false;
		for (int i=0; i<this.size(); i++)
			if (!this.get(i).equals(list.get(i)))
				return false;
		return true;
	}	
}