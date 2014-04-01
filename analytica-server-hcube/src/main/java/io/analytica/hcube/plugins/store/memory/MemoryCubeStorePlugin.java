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
package io.analytica.hcube.plugins.store.memory;

import io.analytica.hcube.HCategoryDictionary;
import io.analytica.hcube.cube.HCube;
import io.analytica.hcube.cube.HCubeBuilder;
import io.analytica.hcube.dimension.HCategory;
import io.analytica.hcube.dimension.HCubeKey;
import io.analytica.hcube.dimension.HTime;
import io.analytica.hcube.impl.CubeStorePlugin;
import io.analytica.hcube.query.HQuery;
import io.analytica.hcube.result.HSerie;
import io.vertigo.kernel.lang.Assertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Impl�mentation m�moire du stockage des Cubes.
 * 
 * @author npiedeloup, pchretien
 * @version $Id: MemoryCubeStorePlugin.java,v 1.11 2013/01/14 16:35:20 npiedeloup Exp $
 */
public final class MemoryCubeStorePlugin implements CubeStorePlugin {
	private final Map<HCubeKey, HCube> queue = new HashMap<>();
	private final Map<HCubeKey, HCube> store = new HashMap<>();

	//	private final boolean ejectTooOld;

	/**
	 * Constructeur.
	 */
	//	public MemoryCubeStorePlugin(@Named("ejectTooOld") boolean ejectTooOld) {
	//		this.ejectTooOld = ejectTooOld;
	//	}

	private int call;

	/** {@inheritDoc} */
	public synchronized void merge(final HCube cube) {
		Assertion.checkNotNull(cube);
		//---------------------------------------------------------------------
		//populate a queue
		merge(cube, cube.getKey(), queue);
		if (queue.size() > 10000) {
			flushQueue();
		}
	}

	//flushing queue into store
	private void flushQueue() {
		for (HCube cube : queue.values()) {
			for (final HCubeKey upCubeKeys : cube.getKey().drillUp()) {
				merge(cube, upCubeKeys, store);
			}
		}
		queue.clear();
		printStats();

	}

	//	private boolean tooOld(final HCubeKey key) {
	//		final Date maxDate;
	//		switch (key.getTime().getDimension()) {
	//			case Minute:
	//				maxDate = new DateBuilder(new Date()).addDays(-3).build(); //pr�cision minute : 3 jours 
	//				break;
	//			case SixMinutes:
	//				maxDate = new DateBuilder(new Date()).addMonths(-2).build(); //pr�cision six minutes : 2 mois 
	//				break;
	//			default:
	//				maxDate = null;
	//		}
	//		if (maxDate != null) {
	//			return key.getTime().getValue().before(maxDate);
	//		}
	//		return false;
	//	}

	private void printStats() {
		if (call++ % 5000 == 0) {
			System.out.println("memStore : " + store.size() + " cubes");
		}
	}

	//On construit un nouveau cube � partir de l'ancien(peut �tre null) et du nouveau.
	private static final void merge(final HCube cube, final HCubeKey cubeKey, Map<HCubeKey, HCube> cubes) {
		final HCubeBuilder cubeBuilder = new HCubeBuilder(cubeKey)//
				.withCube(cube);

		final HCube oldCube = cubes.get(cubeKey);
		if (oldCube != null) {
			cubeBuilder.withCube(oldCube);
		}
		cubes.put(cubeKey, cubeBuilder.build());
	}

	/** {@inheritDoc} */
	public synchronized Map<HCategory, HSerie> findAll(final HQuery query, final HCategoryDictionary categoryDictionary) {

		Assertion.checkNotNull(query);
		Assertion.checkNotNull(categoryDictionary);
		//---------------------------------------------------------------------
		flushQueue();
		//On it�re sur les s�ries index�es par les cat�gories de la s�lection.
		final Map<HCategory, HSerie> cubeSeries = new HashMap<>();

		for (final HCategory category : query.getAllCategories(categoryDictionary)) {
			final List<HCube> cubes = new ArrayList<>();
			for (HTime currentTime : query.getAllTimes()) {
				final HCubeKey cubeKey = new HCubeKey(currentTime, category/*, null*/);
				final HCube cube = store.get(cubeKey);
				//---
				//2 strat�gies possibles : on peut choisir de retourner tous les cubes ou seulement ceux avec des donn�es
				cubes.add(cube == null ? new HCubeBuilder(cubeKey).build() : cube);
				/*if (cube != null) {
					cubes.add(new HCubeBuilder(cubeKey).build());
				}*/
				//---
				currentTime = currentTime.next();
			}
			//A nouveau on peut choisir de retourner toutes les series ou seulement celles avec des donn�es 
			//if (!cubes.isEmpty()) {
			cubeSeries.put(category, new HSerie(category, cubes));
			//}
		}
		printStats();
		return cubeSeries;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized String toString() {
		final StringBuilder sb = new StringBuilder();
		for (final HCube cube : store.values()) {
			sb.append(cube);
			sb.append("\r\n");
		}
		return sb.toString();
	}
}