package dk.stigc.javatunes.audioplayer.other;

import java.util.*;

import dk.stigc.common.StringFunc3; 

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

		if (s == null || s.length()==0)
			throw new RuntimeException("Cannot add null or empty string");
		
		this.add(s);
  	}
	
	public StringList(StringList list)
  	{
  		for (String s: list)
  			this.add(s);
  	}

	public boolean add(String s)
  	{
		if (s == null || s.length()==0)
			throw new RuntimeException("Cannot add null or empty string");
		
  		return super.add(s);
  	}

	@Override
  	public String toString()
  	{
  		return toString("/");
  	}
  	
	public String first()
  	{
  		if (this.size()==0)
  			return "";
		return this.get(0);  			
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

	public boolean hasElmentIgnoreCase(String text)
  	{
  		for (String s: this)
  		{
  			if (s.compareToIgnoreCase(text) == 0)
  				return true;
  		}	
  		return false;
  	}
	
	public boolean containsIgnoreCase(String text)
  	{
  		for (String s: this)
  		{
  			if (StringFunc3.indexOfIgnoreCase(s, text) != -1)
  				return true;
  		}	
  		return false;
  	}

	public int compareToIgnoreCase(StringList that)
	{
		return this.toString().compareToIgnoreCase(that.toString());
	}
	
	public int compareToIgnoreCase(String value)
	{
		return this.toString().compareToIgnoreCase(value);
	}

	public boolean startsWithIgnoreCase(char ch)
	{
		if (size()==0)
			return false;
		return Character.toLowerCase(this.get(0).charAt(0))
				== Character.toLowerCase(ch);
	}

	public void mergeWith(StringList list)
	{
		for (String s : list)
			if (!contains(s))
				add(s);
	}

	public void changeTo(String value)
	{
		if (size() == 1)
		{
			if (!get(0).equals(value))
				set(0, value);
		}
		else
		{
			if (size()>0)
				this.clear();
			add(value);
		}
	}
}