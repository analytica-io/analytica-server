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
package com.kleegroup.analyticaimpl.server.plugins.cubestore.memory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import kasper.kernel.util.Assertion;

import com.kleegroup.analytica.server.data.DataKey;
import com.kleegroup.analytica.server.data.DataType;
import com.kleegroup.analytica.server.data.TimeDimension;
import com.kleegroup.analytica.server.data.TimeSelection;
import com.kleegroup.analytica.server.data.WhatDimension;
import com.kleegroup.analytica.server.data.WhatSelection;
import com.kleegroup.analyticaimpl.server.CubeStorePlugin;
import com.kleegroup.analyticaimpl.server.cube.Cube;
import com.kleegroup.analyticaimpl.server.cube.CubeBuilder;
import com.kleegroup.analyticaimpl.server.cube.CubeKey;
import com.kleegroup.analyticaimpl.server.cube.MetaData;
import com.kleegroup.analyticaimpl.server.cube.Metric;
import com.kleegroup.analyticaimpl.server.cube.TimePosition;
import com.kleegroup.analyticaimpl.server.cube.WhatPosition;

/**
 * Impl�mentation m�moire du stockage des Cubes.
 * @author npiedeloup
 * @version $Id: MemoryCubeStorePlugin.java,v 1.11 2013/01/14 16:35:20 npiedeloup Exp $
 */
public final class MemoryCubeStorePlugin implements CubeStorePlugin {
	private final Comparator<CubeKey> cubeKeyComparator = new CubeKeyComparator();
	private final Map<TimeDimension, Map<WhatDimension, SortedMap<CubeKey, Cube>>> store;
	private String lastProcessIdStored;

	/**
	 * Constructeur.
	 */
	public MemoryCubeStorePlugin() {
		super();
		store = new HashMap<TimeDimension, Map<WhatDimension, SortedMap<CubeKey, Cube>>>();
		for (final TimeDimension timeDimension : TimeDimension.values()) {
			final Map<WhatDimension, SortedMap<CubeKey, Cube>> timeStore = new HashMap<WhatDimension, SortedMap<CubeKey, Cube>>();
			store.put(timeDimension, timeStore);
			for (final WhatDimension whatDimension : WhatDimension.values()) {
				final SortedMap<CubeKey, Cube> whatStore = new TreeMap<CubeKey, Cube>(cubeKeyComparator);
				timeStore.put(whatDimension, whatStore);
			}
		}
	}

	/** {@inheritDoc} */
	public void merge(final Cube lowLevelCube) {
		Assertion.notNull(lowLevelCube);
		//---------------------------------------------------------------------
		//on remonte les axes, le premier sera le plus bas niveau
		TimePosition timePosition = lowLevelCube.getKey().getTimePosition();
		while (timePosition != null) {
			WhatPosition whatPosition = lowLevelCube.getKey().getWhatPosition();
			while (whatPosition != null) {
				final CubeKey storedCubeKey = new CubeKey(timePosition, whatPosition);
				store(lowLevelCube, storedCubeKey);
				//On remonte what
				whatPosition = whatPosition.drillUp();
			}
			//On remonte time
			timePosition = timePosition.drillUp();
		}
	}

	private void store(final Cube cube, final CubeKey cubeKey) {
		final CubeBuilder cubeBuilder = new CubeBuilder(cubeKey);
		cubeBuilder.withCube(cube);

		final Cube oldCube = loadStore(cubeKey).get(cubeKey);
		if (oldCube != null) {
			cubeBuilder.withCube(oldCube);
		}
		loadStore(cubeKey).put(cubeKey, cubeBuilder.build());
	}

	private SortedMap<CubeKey, Cube> loadStore(final CubeKey key) {
		return store.get(key.getTimePosition().getDimension()).get(key.getWhatPosition().getDimension());
	}

	private List<Cube> load(final CubeKey fromKey, final CubeKey toKey) {//fromKey inclus, toKey exclus 
		Assertion.precondition(fromKey.getTimePosition().getDimension().equals(toKey.getTimePosition().getDimension()), "La dimension temporelle du from et du to doit �tre la m�me from:{1} != to:{0}", fromKey.getTimePosition().getDimension(), toKey.getTimePosition().getDimension());
		Assertion.precondition(fromKey.getWhatPosition().getDimension().equals(toKey.getWhatPosition().getDimension()), "La dimension s�mantique du from et du to doit �tre la m�me from:{1} != to:{0}", fromKey.getWhatPosition().getDimension(), toKey.getWhatPosition().getDimension());
		//---------------------------------------------------------------------
		final SortedMap<CubeKey, Cube> dimensionStore = loadStore(fromKey);
		final SortedMap<CubeKey, Cube> subStore = dimensionStore.subMap(fromKey, toKey);
		final List<Cube> result = new ArrayList<Cube>(subStore.values());
		return result;
	}

	/** {@inheritDoc} */
	public List<Cube> load(final TimeSelection timeSelection, final boolean aggregateTime, final WhatSelection whatSelection, final boolean aggregateWhat, final List<DataKey> metrics) {
		//On pr�pare les bornes de temps
		final TimePosition minTime = new TimePosition(timeSelection.getMinValue(), timeSelection.getDimension());
		final TimePosition maxTime = new TimePosition(timeSelection.getMaxValue(), timeSelection.getDimension());
		//on utilise le caract�re 254 (� la fin de l'ascii) pour faire la borne max de recherche : 
		//ainsi l'espace de recherche est [prefix, prefix+(char(154))]
		//cette technique permet d'utiliser le subList plutot qu'un startwith tr�s couteux
		final char lastChar = 254;

		//On remplit une liste de cube avec tous les what voulu
		final List<Cube> allCubes = new ArrayList<Cube>();
		for (final String whatValue : whatSelection.getWhatValues()) {
			final WhatPosition minWhat = new WhatPosition(whatValue, whatSelection.getDimension());
			final WhatPosition maxWhat = new WhatPosition(whatValue + lastChar, whatSelection.getDimension());
			final CubeKey fromKey = new CubeKey(minTime, minWhat);
			final CubeKey toKey = new CubeKey(maxTime, maxWhat);
			allCubes.addAll(load(fromKey, toKey));
		}
		//On prepare un index de metric attendu
		final Set<String> metricNames = new HashSet<String>();
		final Set<String> metaDataNames = new HashSet<String>();
		for (final DataKey dataKey : metrics) {
			if (dataKey.getType() == DataType.metaData) {
				metaDataNames.add(dataKey.getName());
			} else {
				metricNames.add(dataKey.getName());
			}
		}

		//On aggrege les metrics/meta demand�es en fonction des parametres 
		final WhatPosition allWhat = new WhatPosition(WhatDimension.SEPARATOR, whatSelection.getDimension());
		final SortedMap<CubeKey, CubeBuilder> cubeBuilderIndex = new TreeMap<CubeKey, CubeBuilder>(cubeKeyComparator);
		for (final Cube cube : allCubes) {
			//Si on aggrege sur une dimension, on la fige plutot que prendre la position de la donn�e
			final WhatPosition useWhat = aggregateWhat ? allWhat : cube.getKey().getWhatPosition();
			final TimePosition useTime = aggregateTime ? minTime : cube.getKey().getTimePosition();
			final CubeBuilder cubeBuilder = obtainCubeBuilder(useTime, useWhat, cubeBuilderIndex);

			for (final Metric metric : cube.getMetrics()) {
				if (metricNames.contains(metric.getName())) {
					cubeBuilder.withMetric(metric);
				}
			}
			for (final MetaData metaData : cube.getMetaDatas()) {
				if (metaDataNames.contains(metaData.getName())) {
					cubeBuilder.withMetaData(metaData);
				}
			}
		}
		final List<Cube> cube = new ArrayList<Cube>(cubeBuilderIndex.size());
		for (final CubeBuilder cubeBuilder : cubeBuilderIndex.values()) {
			cube.add(cubeBuilder.build());
		}
		return cube;
	}

	private CubeBuilder obtainCubeBuilder(final TimePosition timePosition, final WhatPosition whatPosition, final SortedMap<CubeKey, CubeBuilder> timeIndex) {
		final CubeKey key = new CubeKey(timePosition, whatPosition);
		CubeBuilder cubeBuilder = timeIndex.get(key);
		if (cubeBuilder == null) {
			cubeBuilder = new CubeBuilder(key);
			timeIndex.put(key, cubeBuilder);
		}
		return cubeBuilder;
	}

	/** {@inheritDoc} */
	public List<TimePosition> loadSubTimePositions(final TimeSelection timeSelection) {
		//		final TimePosition minTime = new TimePosition(timeSelection.getMinValue(), timeSelection.getDimension());
		//		final TimePosition maxTime = new TimePosition(timeSelection.getMaxValue(), timeSelection.getDimension());
		// TODO Auto-generated method stub

		return Collections.emptyList();
	}

	/** {@inheritDoc} */
	public List<WhatPosition> loadSubWhatPositions(final TimeSelection timeSelection, final WhatSelection whatSelection) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	/** {@inheritDoc} */
	public List<DataKey> loadDataKeys(final TimeSelection timeSelection, final WhatSelection whatSelection) {
		// TODO Auto-generated method stub
		return Collections.emptyList();
	}

	/** {@inheritDoc} */
	public String loadLastProcessIdStored() {
		return lastProcessIdStored;
	}

	/** {@inheritDoc} */
	public void saveLastProcessIdStored(final String newLastProcessIdStored) {
		lastProcessIdStored = newLastProcessIdStored;
	}
}