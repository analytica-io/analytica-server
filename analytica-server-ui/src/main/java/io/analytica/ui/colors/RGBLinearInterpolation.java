/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidière - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.ui.colors;

import java.awt.Color;

/**
 * Interpolation linaire des couleurs.
 * @author npiedeloup
 * @version $Id: $
 */
public class RGBLinearInterpolation extends AbstractRGBPointToPointInterpolation {

	/** {@inheritDoc} */
	@Override
	public String getInterpolationCode() {
		return "LINEAR";
	}

	/** {@inheritDoc} */
	@Override
	protected Color colorInterpolation(final double t, final Color c1, final Color c2, final Color c3, final Color c4) {
		final int red = (int) Math.round(linear(t, c2.getRed(), c3.getRed()));
		final int green = (int) Math.round(linear(t, c2.getGreen(), c3.getGreen()));
		final int blue = (int) Math.round(linear(t, c2.getBlue(), c3.getBlue()));
		return new Color(red, green, blue);
	}

	private static double linear(final double t, final double p1, final double p2) {
		return p1 + (p2 - p1) * t;
	}

}
