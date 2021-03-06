/*
 * Copyright 2003 (C) Devon Jones
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * $Id$
 */
 package pcgen.gui2.doomsdaybook;

/**
 *
 * @author  devon
 */
public class XMLFilter implements java.io.FilenameFilter
{
	/** Creates a new instance of XMLFilter */
	public XMLFilter()
	{
		// Empty Constructor
	}

	/**
	 * Returns true if file matches *.xml
	 * 
	 * @param file 
	 * @param str 
	 * @return true if filter matches *.xml
	 */
	@Override
	public boolean accept(java.io.File file, String str)
	{
		return str.matches(".*\\.xml$");
	}
}
