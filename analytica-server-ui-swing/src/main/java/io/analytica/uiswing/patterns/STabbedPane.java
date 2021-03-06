/**
 * Analytica - beta version - Systems Monitoring Tool
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiére - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.analytica.uiswing.patterns;

import java.awt.Color;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import mswing.print.MPrintInformations;

/**
 * TabbedPane SAE (panel avec onglets).<br>
 * On ne laisse qu'un constructeur qui positionne TAB_PLACEMENT é TOP et TAB_LAYOUT_POLICY é SCROLL_TAB_LAYOUT,
 * comportement par défaut désiré dans le client SAE. <br>
 * De plus on définit une gestion des couleurs par défaut dans le SAE basés sur les propriétés
 * TabbedPane.passiveForeground, TabbedPane.activeForeground et TabbedPane.passiveBackground qui doivent étre
 * initialisées par le client (sinon les couleurs par défaut sont utilisées : noir sur fond gris).
 * @version $Id: STabbedPane.java,v 1.1 2012/01/13 13:43:55 npiedeloup Exp $
 * @author Antoine GERARD
 */
public class STabbedPane extends JTabbedPane implements MPrintInformations {

	private static final long serialVersionUID = 933319440473168430L;
	private static final Color ONGLET_PASSIF_FOREGROUND_COLOR = UIManager.getColor("TabbedPane.passiveForeground");
	private static final Color ONGLET_ACTIF_FOREGROUND_COLOR = UIManager.getColor("TabbedPane.activeForeground");
	private static final Color ONGLET_PASSIF_BACKGROUND_COLOR = UIManager.getColor("TabbedPane.passiveBackground");

	public STabbedPane() {
		super(TOP, SCROLL_TAB_LAYOUT);
		// on ajoute un change listener pour mettre é jour les couleurs é chaque changement de l'onglet ouvert.
		// sinon le look and feel les changent é chaque sélection
		addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent event) {
				updateColors();
			}
		});
		updateColors();
	}

	/**
	 * Met é jour les couleurs des onglets en fonction de l'onglet ouvert.
	 */
	private void updateColors() {
		final int selectedIndex = getSelectedIndex();
		final int nbTabs = getTabCount();
		for (int index = 0; index < nbTabs; index++) {
			if (selectedIndex != index) {
				setForegroundAt(index, ONGLET_PASSIF_FOREGROUND_COLOR);
				setBackgroundAt(index, ONGLET_PASSIF_BACKGROUND_COLOR);
			} else {
				setForegroundAt(index, ONGLET_ACTIF_FOREGROUND_COLOR);
			}
		}
	}

	/**
	 * Cette méthode est appelée par toutes les autres méthodes permettant d'ajouter un onglet. On la surcharge pour
	 * mettre é jour les couleurs é chaque ajout d'onglet.
	 *
	 * @param title String
	 * @param icon Icon
	 * @param component Component
	 * @param tip String
	 * @param index int
	 */
	@Override
	public void insertTab(final String title, final Icon icon, final Component component, final String tip, final int index) {
		super.insertTab(title, icon, component, tip, index);
		updateColors();
	}

	/**
	 * Retourne le titre é inclure dans l'impression/export.
	 * @return java.lang.String
	 */
	@Override
	public String getPrintTitle() {
		final int selectedIndex = getSelectedIndex();
		if (selectedIndex != -1) {
			return getTitleAt(selectedIndex);
		}
		return null;
	}
}
