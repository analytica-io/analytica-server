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
package io.analytica.hcube.cube;

import io.analytica.hcube.dimension.HCubeKey;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collection;
import java.util.Map;

/**
 * Un cube contient :
 *  - des m�triques nomm�es
 *  	exemple : temps r�ponse, nombre de mails envoy�s
 *  - des m�tadonn�es 
 *  	exemple : tags, users
 *  
 * @author npiedeloup, pchretien
 * @version $Id: Cube.java,v 1.6 2012/10/16 13:34:49 pchretien Exp $
 */
public final class HCube implements HVirtualCube {
	/**
	 * Identifiant du cube : un cube est localis� dans le temps et l'espace (axe fonctionnel).
	 */
	private final HCubeKey cubeKey;
	private final Map<HMetricKey, HMetric> metrics;

	HCube(final HCubeKey cubeKey, final Map<HMetricKey, HMetric> metrics) {
		Assertion.checkNotNull(cubeKey);
		Assertion.checkNotNull(metrics);
		//---------------------------------------------------------------------
		this.cubeKey = cubeKey;
		this.metrics = metrics;
	}

	public HCubeKey getKey() {
		return cubeKey;
	}

	/** {@inheritDoc} */
	public HMetric getMetric(final HMetricKey metricKey) {
		Assertion.checkNotNull(metricKey);
		//---------------------------------------------------------------------
		return metrics.get(metricKey);
	}

	/** {@inheritDoc} */
	public Collection<HMetric> getMetrics() {
		return metrics.values();
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder()//
				.append(cubeKey).append("\n\tmetrics:{");
		for (final HMetric metric : getMetrics()) {
			sb.append("\n\t\t ").append(metric);
		}
		if (!getMetrics().isEmpty()) {
			sb.append("\n\t");
		}
		sb.append("}");
		return sb.toString();
	}
}
