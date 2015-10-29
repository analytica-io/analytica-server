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
package io.analytica.server.store;

import io.analytica.api.KProcess;
import io.vertigo.lang.Plugin;

import java.util.List;

/**
 * Plugin g�rant le stockage des process.
 * @author npiedeloup
 * @version $Id: ProcessStorePlugin.java,v 1.2 2012/04/06 16:06:46 npiedeloup Exp $
 */
public interface ProcessStorePlugin extends Plugin {
	/**
	 * Ajout un processus identifi�.
	 * @param process processus identifi�.
	 */
	void add(KProcess process);

	/**
	 * Liste des process suivant.
	 * @param systemName Nom du system
	 * @param lastId Dernier id de process charg� (exclus du resultat)
	 * @param maxRow Nombre de ligne max
	 * @return Liste des process suivant
	 */
	List<Identified<KProcess>> getProcess(final String appName, final String lastId, final Integer maxRow);

	/**
	 * Liste tous les applications disponible dans le store
	 * */
	List<String> getApps();
}