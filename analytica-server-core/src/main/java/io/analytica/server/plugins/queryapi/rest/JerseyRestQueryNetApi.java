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
package io.analytica.server.plugins.queryapi.rest;

import io.analytica.hcube.HCubeManager;
import io.analytica.hcube.cube.HMetric;
import io.analytica.hcube.dimension.HCategory;
import io.analytica.hcube.query.HQuery;
import io.analytica.hcube.result.HResult;
import io.analytica.server.ServerManager;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.injector.Injector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Api REST avec jersey de requetage des cubes.
 * @author npiedeloup
 * @version $Id: $
 */
@Path("/query")
public class JerseyRestQueryNetApi {
	private final String dTimeTo = "NOW+1h";
	private final String dTimeFrom = "NOW-8h";
	private final String dTimeDim = "Minute";
	private final String dDatas = "duration:mean";

	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	@Inject
	private ServerManager serverManager;
	@Inject
	private HCubeManager cubeManager;

	/**
	 * Constructeur simple, pour instanciation par Jersey.
	 */
	public JerseyRestQueryNetApi() {
		new Injector().injectMembers(this, Home.getComponentSpace());
	}

	@GET
	@Path("/timeLine/{type}{subcategories:(/.+?)?}")
	//le type est obligatoire les sous cat�gories (s�par�es par /) sont optionnelles
	@Produces(MediaType.APPLICATION_JSON)
	public String getTimeLine(@QueryParam("timeFrom") @DefaultValue(dTimeFrom) final String timeFrom, @QueryParam("timeTo") @DefaultValue(dTimeTo) final String timeTo, @DefaultValue(dTimeDim) @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue(dDatas) @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, false);
		final HResult result = serverManager.execute(query);
		final List<String> dataKeys = Arrays.asList(datas.split(";"));
		final List<TimedDataSerie> dataSeries = Utils.loadDataSeriesByTime(result, dataKeys);
		return gson.toJson(dataSeries);
	}

	@GET
	@Path("/categoryLine/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAggregatedDataByCategory(@QueryParam("timeFrom") @DefaultValue(dTimeFrom) final String timeFrom, @QueryParam("timeTo") @DefaultValue(dTimeTo) final String timeTo, @DefaultValue(dTimeDim) @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue(dDatas) @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, true);
		final HResult result = serverManager.execute(query);
		final List<String> dataKeys = Arrays.asList(datas.split(";"));
		final List<DataSerie> dataSeries = Utils.loadDataSeriesByCategory(result, dataKeys);
		return gson.toJson(dataSeries);
	}

	@GET
	@Path("/metricLine/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAggregatedDataByTimeAndCategory(@QueryParam("timeFrom") @DefaultValue(dTimeFrom) final String timeFrom, @QueryParam("timeTo") @DefaultValue(dTimeTo) final String timeTo, @DefaultValue(dTimeDim) @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue(dDatas) @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, true);
		final HResult result = serverManager.execute(query);
		final List<String> dataKeys = Arrays.asList(datas.split(";"));
		final List<DataSerie> dataSeries = Utils.loadDataSeriesByCategory(result, dataKeys);
		return gson.toJson(dataSeries);
	}

	@GET
	@Path("/categories")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCategories() {
		return gson.toJson(cubeManager.getCategoryDictionary().getAllRootCategories());
	}

	@GET
	@Path("/categories/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCategories(@PathParam("type") final String type, @PathParam("subcategories") final String subCategories) {
		final HCategory hCategory = subCategories.isEmpty() ? new HCategory(type) : new HCategory(type, subCategories.split("/"));
		return gson.toJson(cubeManager.getCategoryDictionary().getAllSubCategories(hCategory));
	}

	@GET
	@Path("/metrics/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getMetrics(@PathParam("type") final String type, @PathParam("subcategories") final String subCategories) {
		final HCategory hCategory = subCategories.isEmpty() ? new HCategory(type) : new HCategory(type, subCategories.split("/"));
		final HQuery query = Utils.createQuery("NOW-1m", "NOW", "Month", type, subCategories, false);
		final HResult result = serverManager.execute(query);
		final Collection<HMetric> metrics = result.getSerie(hCategory).getMetrics();
		final List<Map<String, Object>> metricsName = new ArrayList<>();
		for (final HMetric metric : metrics) {
			final Map<String, Object> metricName = new HashMap<>();
			metricsName.add(metricName);
			metricName.put("name", metric.getKey().id());
			final List<String> values = new ArrayList<>();
			metricName.put("type", values);
			values.add("count");
			values.add("mean");
			values.add("min");
			values.add("max");
			values.add("sum");
			values.add("sqrSum");
			values.add("stdDev");
			if (metric.getKey().isClustered()) {
				values.add("clustered");
			}
		}
		return gson.toJson(metricsName);
	}

	//	@GET
	//	@Path("/bollinger/{type}{subcategories:(/.+?)?}")
	//	@Produces(MediaType.APPLICATION_JSON)
	//	public String getBollingerBands(@QueryParam("timeFrom") @DefaultValue(dTimeFrom) final String timeFrom, @QueryParam("timeTo") @DefaultValue(dTimeTo) final String timeTo, @DefaultValue(dTimeDim) @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue(dDatasMult) @QueryParam("datas") final String datas) {
	//		final HResult result = Utils.resolveQuery(timeFrom, timeTo, timeDim, category, false);
	//		final Map<String, List<DataPoint>> pointsMap = Utils.loadBollingerBands(result, datas);
	//
	//		return gson.toJson(pointsMap);
	//	}

	@GET
	@Path("/stackedDatas/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getAllCategoriesToStack(@QueryParam("timeFrom") @DefaultValue("NOW-12h") final String timeFrom, @QueryParam("timeTo") @DefaultValue("NOW+2h") final String timeTo, @DefaultValue("Hour") @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue("duration:count") @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, true);
		final HResult result = serverManager.execute(query);

		return gson.toJson(Utils.loadDataPointsStackedByCategory(result, datas));
	}

	@GET
	@Path("/tableSparkline/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getTableSparklineDatas(@QueryParam("timeFrom") @DefaultValue("NOW-12h") final String timeFrom, @QueryParam("timeTo") @DefaultValue("NOW+2h") final String timeTo, @DefaultValue("Hour") @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue("duration:count") @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, true);
		final HResult result = serverManager.execute(query);

		return gson.toJson(Utils.getSparklinesTableDatas(result, datas));
	}

	@GET
	@Path("/tablePunchcard/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPunchCardDatas(@QueryParam("timeFrom") @DefaultValue("NOW-240h") final String timeFrom, @QueryParam("timeTo") @DefaultValue("NOW") final String timeTo, @DefaultValue("Hour") @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue("duration:count") @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, false);
		final HResult result = serverManager.execute(query);
		return gson.toJson(Utils.getPunchCardDatas(result, datas));
	}

	@GET
	@Path("/faketablePunchcard/{type}{subcategories:(/.+?)?}")
	@Produces(MediaType.APPLICATION_JSON)
	public String getPunchCardFakeDatas(@QueryParam("timeFrom") @DefaultValue("NOW-240h") final String timeFrom, @QueryParam("timeTo") @DefaultValue("NOW") final String timeTo, @DefaultValue("Hour") @QueryParam("timeDim") final String timeDim, @PathParam("type") final String type, @PathParam("subcategories") final String subCategories, @DefaultValue("duration:count") @QueryParam("datas") final String datas) {
		final HQuery query = Utils.createQuery(timeFrom, timeTo, timeDim, type, subCategories, false);
		final HResult result = serverManager.execute(query);
		return gson.toJson(Utils.getPunchCardFakeDatas(result, datas));
	}

	/**
	 * @return Version de l'api
	 */
	@GET
	@Path("/version")
	@Produces(MediaType.TEXT_PLAIN)
	public String getVersion() {
		return "1.0.0";
	}

}