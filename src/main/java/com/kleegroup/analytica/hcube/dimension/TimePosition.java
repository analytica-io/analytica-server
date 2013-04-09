/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidi�re - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * This program is free software; you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation;
 * either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program;
 * if not, see <http://www.gnu.org/licenses>
 */
package com.kleegroup.analytica.hcube.dimension;

import java.util.Date;

import com.kleegroup.analytica.hcube.Identity;

/**
 * @author npiedeloup
 * @version $Id: TimePosition.java,v 1.2 2012/04/17 09:11:15 pchretien Exp $
 */
public final class TimePosition extends Identity implements Position<TimeDimension, TimePosition> {
	private final TimeDimension dimension;
	private final Date date;

	public TimePosition(final Date date, final TimeDimension timeDimension) {
		super("Time:[" + timeDimension.name() + "]" + timeDimension.reduce(date).getTime());
		//Assertion.notNull(timeDimension); inutil
		//---------------------------------------------------------------------
		this.dimension = timeDimension;
		this.date = timeDimension.reduce(date);
	}

	/** {@inheritDoc} */
	public TimePosition drillUp() {
		final TimeDimension upTimeDimension = dimension.drillUp();
		return upTimeDimension != null ? new TimePosition(date, upTimeDimension) : null;
	}

	/** {@inheritDoc} */
	public TimeDimension getDimension() {
		return dimension;
	}

	public Date getValue() {
		return date;
	}
}