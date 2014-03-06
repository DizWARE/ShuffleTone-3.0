package com.DizWARE.ShuffleTone.Others;

import java.io.Serializable;

public class FilterType implements Comparable<FilterType>, Serializable 
{
	private static final long serialVersionUID = 1L;
	
	public int category;
	public String filter;
	public boolean selected = false;
	
	/***
	 * Creates a new filter type
	 */
	public FilterType(int category, String filter)
	{
		this.category = category;
		this.filter = filter;
	}
	
	/***
	 * If the filter types are the same, return true
	 */
	@Override public boolean equals(Object o) {
		FilterType other = (FilterType)o;
		return (category == other.category) && (filter.equalsIgnoreCase(other.filter));
	}

	/***
	 * Creates a natural order that matches the natural order of the filter strings
	 */
	@Override public int compareTo(FilterType arg0) {
		return filter.compareToIgnoreCase(arg0.filter);
	}
	
	
}
