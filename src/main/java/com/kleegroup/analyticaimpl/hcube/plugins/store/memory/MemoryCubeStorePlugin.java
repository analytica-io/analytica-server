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
package com.kleegroup.analyticaimpl.hcube.plugins.store.memory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kasper.kernel.exception.KRuntimeException;
import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.hcube.cube.Cube;
import com.kleegroup.analytica.hcube.cube.CubeBuilder;
import com.kleegroup.analytica.hcube.dimension.CubePosition;
import com.kleegroup.analytica.hcube.dimension.TimePosition;
import com.kleegroup.analytica.hcube.dimension.WhatPosition;
import com.kleegroup.analytica.hcube.query.Query;
import com.kleegroup.analyticaimpl.hcube.CubeStorePlugin;

/**
 * Impl�mentation m�moire du stockage des Cubes.
 * @author npiedeloup
 * @version $Id: MemoryCubeStorePlugin.java,v 1.11 2013/01/14 16:35:20 npiedeloup Exp $
 */
final class MemoryCubeStorePlugin implements CubeStorePlugin {
	private final Map<CubePosition, Cube> store = new HashMap<CubePosition, Cube>();

	//	private String lastProcessIdStored;

	/**
	 * Constructeur.
	 */
	public MemoryCubeStorePlugin() {
		//
	}

	/** {@inheritDoc} */
	public void merge(final Cube lowLevelCube) {
		Assertion.notNull(lowLevelCube);
		//---------------------------------------------------------------------
		for (CubePosition upCubePosition : lowLevelCube.getPosition().drillUp()) {
			Cube cube = merge(lowLevelCube, upCubePosition);
			store.put(cube.getPosition(), cube);
		}
	}

	//On construit un nouveau cube � partir de l'ancien(peut �tre null) et du nouveau.
	private final Cube merge(final Cube cube, final CubePosition cubePosition) {
		final CubeBuilder cubeBuilder = new CubeBuilder(cubePosition)//
				.withCube(cube);

		final Cube oldCube = store.get(cubePosition);
		if (oldCube != null) {
			cubeBuilder.withCube(oldCube);
		}
		return cubeBuilder.build();
	}

	//	/** {@inheritDoc} */
	public List<Cube> findAll(Query query) {
		//On pr�pare les bornes de temps
		final TimePosition minTimePosition = query.getMinTimePosition();
		final TimePosition maxTimePosition = query.getMaxTimePosition();
		final WhatPosition whatPosition = query.getWhatPosition();

		//S�curit� pour �viter une boucle infinie
		List<Cube> cubes = new ArrayList<Cube>();

		int loops = 0;
		TimePosition currentTimePosition = minTimePosition;
		do {
			CubePosition cubePosition = new CubePosition(currentTimePosition, whatPosition);
			Cube cube = store.get(cubePosition);
			//---
			cubes.add(cube == null ? new CubeBuilder(cubePosition).build() : cube);
			//---
			currentTimePosition = currentTimePosition.next();
			loops++;
			if (loops > 1000) {
				throw new KRuntimeException("Segment temporel trop grand : plus de 1000 positions");
			}
		} while (currentTimePosition.getValue().before(maxTimePosition.getValue()));

		return cubes;
	}

	//	/** {@inheritDoc} */
	//	public List<Cube> load(final Query query, final boolean aggregateTime, final boolean aggregateWhat) {
	//		//On pr�pare les bornes de temps
	//		final TimePosition minTimePosition = query.getMinTimePosition();
	//		final TimePosition maxTimePosition = query.getMaxTimePosition();
	//		final List<String> what = query.getWhat();
	//		//On remplit une liste de cube avec tous les what voulu.
	//		final List<Cube> allCubes = new ArrayList<Cube>();
	//		final WhatPosition whatPosition = new WhatPosition(what);
	//		final CubePosition fromKey = new CubePosition(minTimePosition, whatPosition);
	//		final CubePosition toKey = new CubePosition(maxTimePosition, whatPosition);
	//
	//		//On prepare un index de metric attendu
	//		final Set<MetricKey> metricKeys = new HashSet<MetricKey>();
	//		for (final DataKey dataKey : query.getKeys()) {
	//			metricKeys.add(dataKey.getMetricKey());
	//		}
	//
	//		//On aggrege les metrics/meta demand�es en fonction des parametres 
	//		final WhatPosition allWhat = new WhatPosition(Collections.<String> emptyList());
	//		final SortedMap<CubePosition, CubeBuilder> cubeBuilderIndex = new TreeMap<CubePosition, CubeBuilder>(cubeKeyComparator);
	//		for (final Cube cube : allCubes) {
	//			//Si on aggrege sur une dimension, on la fige plutot que prendre la position de la donn�e
	//			final WhatPosition useWhat = aggregateWhat ? allWhat : cube.getPosition().getWhatPosition();
	//			final TimePosition useTime = aggregateTime ? minTimePosition : cube.getPosition().getTimePosition();
	//			final CubePosition key = new CubePosition(useTime, useWhat);
	//
	//			final CubeBuilder cubeBuilder = obtainCubeBuilder(key, cubeBuilderIndex);
	//
	//			for (final Metric metric : cube.getMetrics()) {
	//				if (metricKeys.contains(metric.getKey())) {
	//					cubeBuilder.withMetric(metric);
	//				}
	//			}
	//		}
	//		final List<Cube> cubes = new ArrayList<Cube>(cubeBuilderIndex.size());
	//		for (final CubeBuilder cubeBuilder : cubeBuilderIndex.values()) {
	//			cubes.add(cubeBuilder.build());
	//		}
	//		return cubes;
	//	}
	//
	//	private CubeBuilder obtainCubeBuilder(final CubePosition cubePosition, final SortedMap<CubePosition, CubeBuilder> timeIndex) {
	//		CubeBuilder cubeBuilder = timeIndex.get(cubePosition);
	//		if (cubeBuilder == null) {
	//			cubeBuilder = new CubeBuilder(cubePosition);
	//			timeIndex.put(cubePosition, cubeBuilder);
	//		}
	//		return cubeBuilder;
	//	}
	//
	//	/** {@inheritDoc} */
	//	public String loadLastProcessIdStored() {
	//		return lastProcessIdStored;
	//	}
	//
	//	/** {@inheritDoc} */
	//	public void saveLastProcessIdStored(final String newLastProcessIdStored) {
	//		lastProcessIdStored = newLastProcessIdStored;
	//	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Cube cube : store.values()) {
			sb.append(cube);
			sb.append("\r\n");
		}
		return sb.toString();
	}

}
