/*
 * Copyright 2008 (C) Thomas Parker <thpr@users.sourceforge.net>
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
 */
package plugin.lsttokens;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

import pcgen.cdom.base.CDOMObject;
import pcgen.cdom.base.Constants;
import pcgen.cdom.enumeration.ListKey;
import pcgen.core.PCClass;
import pcgen.core.SpecialAbility;
import pcgen.core.prereq.Prerequisite;
import pcgen.rules.context.Changes;
import pcgen.rules.context.LoadContext;
import pcgen.rules.persistence.token.AbstractToken;
import pcgen.rules.persistence.token.CDOMPrimaryToken;
import pcgen.util.Logging;

public class SabLst extends AbstractToken implements
		CDOMPrimaryToken<CDOMObject>
{

	@Override
	public String getTokenName()
	{
		return "SAB";
	}

	public boolean parse(LoadContext context, CDOMObject obj, String value)
	{
		return parseSpecialAbility(context, obj, value);
	}

	/**
	 * This method sets the special abilities granted by this [object]. For
	 * efficiency, avoid calling this method except from I/O routines.
	 * 
	 * @param obj
	 *            the PObject that is to receive the new SpecialAbility
	 * @param aString
	 *            String of special abilities delimited by pipes
	 * @param level
	 *            int level at which the ability is gained
	 */
	public boolean parseSpecialAbility(LoadContext context, CDOMObject obj,
			String aString)
	{
		if (isEmpty(aString) || hasIllegalSeparator('|', aString))
		{
			return false;
		}

		StringTokenizer tok = new StringTokenizer(aString, Constants.PIPE);

		String firstToken = tok.nextToken();
		if (firstToken.startsWith("PRE") || firstToken.startsWith("!PRE"))
		{
			Logging.log(Logging.LST_ERROR, "Cannot have only PRExxx subtoken in "
					+ getTokenName());
			return false;
		}
		
		boolean foundClear = false;

		if (Constants.LST_DOT_CLEAR.equals(firstToken))
		{
			context.getObjectContext().removeList(obj, ListKey.SAB);
			if (!tok.hasMoreTokens())
			{
				return true;
			}
			firstToken = tok.nextToken();
			foundClear = true;
		}

		if (firstToken.startsWith("PRE") || firstToken.startsWith("!PRE"))
		{
			Logging.log(Logging.LST_ERROR,
					"Cannot use PREREQs when using .CLEAR in "
							+ getTokenName());
			return false;
		}

		if (Constants.LST_DOT_CLEAR.equals(firstToken))
		{
			Logging.log(Logging.LST_ERROR, "SA tag confused by redundant '.CLEAR'"
					+ aString);
			return false;
		}

		SpecialAbility sa = new SpecialAbility(firstToken);

		if (!tok.hasMoreTokens())
		{
			sa.setName(firstToken);
			context.getObjectContext().addToList(obj, ListKey.SAB, sa);
			return true;
		}

		StringBuilder saName = new StringBuilder();
		saName.append(firstToken);

		String token = tok.nextToken();
		while (true)
		{
			if (Constants.LST_DOT_CLEAR.equals(token))
			{
				Logging.log(Logging.LST_ERROR, "SA tag confused by '.CLEAR' as a "
						+ "middle token: " + aString);
				return false;
			}
			else if (token.startsWith("PRE") || token.startsWith("!PRE"))
			{
				break;
			}
			else
			{
				saName.append(Constants.PIPE).append(token);
				// sa.addVariable(FormulaFactory.getFormulaFor(token));
			}

			if (!tok.hasMoreTokens())
			{
				// No prereqs, so we're done
				// CONSIDER This is a HACK and not the long term strategy of SA:
				sa.setName(saName.toString());
				context.getObjectContext().addToList(obj, ListKey.SAB, sa);
				return true;
			}
			token = tok.nextToken();
		}
		// CONSIDER This is a HACK and not the long term strategy of SA:
		sa.setName(saName.toString());

		if (foundClear)
		{
			Logging.log(Logging.LST_ERROR,
					"Cannot use PREREQs when using .CLEAR and a Special Ability in "
							+ getTokenName());
			return false;
		}

		while (true)
		{
			Prerequisite prereq = getPrerequisite(token);
			if (prereq == null)
			{
				Logging.log(Logging.LST_ERROR, "   (Did you put Abilities after the "
						+ "PRExxx tags in " + getTokenName() + ":?)");
				return false;
			}
			/*
			 * The following subkey is required in order to give context to the
			 * variables as they are calculated (make the context the current
			 * class, so that items like Class Level can be correctly
			 * calculated).
			 */
			if (obj instanceof PCClass && "var".equals(prereq.getKind()))
			{
				prereq.setSubKey("CLASS:" + obj.getKeyName());
			}
			sa.addPrerequisite(prereq);
			if (!tok.hasMoreTokens())
			{
				break;
			}
			token = tok.nextToken();
		}
		context.getObjectContext().addToList(obj, ListKey.SAB, sa);
		return true;
	}

	public String[] unparse(LoadContext context, CDOMObject obj)
	{
		Changes<SpecialAbility> changes = context.getObjectContext()
				.getListChanges(obj, ListKey.SAB);
		Collection<SpecialAbility> added = changes.getAdded();
		List<String> list = new ArrayList<String>();
		if (changes.includesGlobalClear())
		{
			list.add(Constants.LST_DOT_CLEAR);
		}
		else if (added == null || added.isEmpty())
		{
			// Zero indicates no Token (and no global clear, so nothing to do)
			return null;
		}
		if (added != null)
		{
			for (SpecialAbility ab : added)
			{
				StringBuilder sb = new StringBuilder();
				sb.append(ab.getLSTformat());
				if (ab.hasPrerequisites())
				{
					sb.append(Constants.PIPE);
					sb.append(getPrerequisiteString(context, ab
							.getPrerequisiteList()));
				}
				list.add(sb.toString());
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public Class<CDOMObject> getTokenClass()
	{
		return CDOMObject.class;
	}
}
