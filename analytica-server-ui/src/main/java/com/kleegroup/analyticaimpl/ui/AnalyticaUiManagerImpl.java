package com.kleegroup.analyticaimpl.ui;

import javax.inject.Inject;

import com.kleegroup.analytica.restserver.RestServerManager;

/**
 * Impl�mentation du tableau de bord.
 * 
 * @author pchretien
 * @version $Id: SpacesManagerImpl.java,v 1.2 2013/05/15 17:35:48 pchretien Exp $
 */
public final class AnalyticaUiManagerImpl implements AnalyticaUiManager {

	/**
	 * Constructeur simple pour instanciation par jersey.
	 * @param restServerManager Manager de server Rest
	 */
	@Inject
	public AnalyticaUiManagerImpl(final RestServerManager restServerManager) {
		restServerManager.addResourceHandler(DashboardServices.class);
		restServerManager.addStaticPath("/static/", "/static/");
		restServerManager.addStaticPath("/pages/", "/");
	}

}