/**
 * 
 */
package br.unicamp.ic.controller;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import br.unicamp.ic.microservices.graphs.MicroservicesGraphUtil;
import br.unicamp.ic.microservices.metrics.GiniCoefficientResult;
import br.unicamp.ic.microservices.metrics.GiniSeries;
import br.unicamp.ic.microservices.metrics.Metric;
import br.unicamp.ic.microservices.metrics.Metric.MetricType;
import br.unicamp.ic.microservices.model.Microservice;
import br.unicamp.ic.microservices.model.MicroservicesApplication;

/**
 * @author Daniel R. F. Apolinario
 *
 */
@RestController
@RequestMapping("calculateGiniSeries")
public class CalculateGiniSeriesController {

	private static final String searchFolder = "/home/daniel/Documentos/mestrado-2018/projeto/grafos-dependencia/";

	private static final String calculateGiniIndexURL = "http://localhost:8000/calculate-gini-index/";

	@Autowired
	private Environment environment;

	private RestTemplate restTemplate;

	public CalculateGiniSeriesController() {
		this.restTemplate = new RestTemplate();
	}

	@PostMapping
	public List<MicroservicesApplication> calculateGiniCoefficient() {

		List<MicroservicesApplication> msaList = new ArrayList<MicroservicesApplication>();

		msaList = calculateGiniSeriesData();

		// exportTestResultsToJson(app.getName(), testResultList);
		return msaList;
	}

	private List<MicroservicesApplication> calculateGiniSeriesData() {
		List<MicroservicesApplication> microservicesApplicationList = null;
		microservicesApplicationList = getAllMicroservicesApplication();
		for (MicroservicesApplication msApp : microservicesApplicationList) {
			calculateGiniForMicroservices(msApp.getMicroservices());
			calculateGiniForApplication(msApp);
		}
		return microservicesApplicationList;
	}

	/**
	 * @param msApp
	 */
	private void calculateGiniForApplication(MicroservicesApplication msApp) {
		int sizeOfSeries = getSizeOfSeries(msApp);
		List<List> adsSeriesData = new ArrayList<>();
		List<List> aisSeriesData = new ArrayList<>();
		for (int i = 0; i < sizeOfSeries; i++) {
			List<Object> adsMetricValues = new ArrayList<Object>();
			adsSeriesData.add(adsMetricValues);
			List<Object> aisMetricValues = new ArrayList<Object>();
			aisSeriesData.add(aisMetricValues);
		}
		for (Microservice microservice : msApp.getMicroservices()) {
			Object[] adsMetricValues = getMetricValues(microservice, MetricType.ADS);
			int i = 0;
			for (Object value : adsMetricValues) {
				List metricValuesList = adsSeriesData.get(i);
				metricValuesList.add(value);
				i += 1;
			}
			Object[] aisMetricValues = getMetricValues(microservice, MetricType.AIS);
			int j = 0;
			for (Object value : aisMetricValues) {
				List metricValuesList = aisSeriesData.get(j);
				metricValuesList.add(value);
				j += 1;
			}

		}

		// identify invalid values when microservices were not created yet and
		// their metric values are zero.
		removeInvalidMetricValues(adsSeriesData, aisSeriesData);

		GiniSeries adsGiniSeries = calculateGiniIndexBetweenMicroservices(adsSeriesData, MetricType.ADS);
		GiniSeries aisGiniSeries = calculateGiniIndexBetweenMicroservices(aisSeriesData, MetricType.AIS);

		msApp.addGiniSeries(adsGiniSeries);
		msApp.addGiniSeries(aisGiniSeries);
	}

	/**
	 * @param adsSeriesData
	 * @param aisSeriesData
	 */
	private void removeInvalidMetricValues(List<List> adsSeriesData, List<List> aisSeriesData) {
		int releasesListSize = adsSeriesData.size();
		for (int i = 0; i < releasesListSize; i++) {
			List adsValuesList = adsSeriesData.get(i);
			List aisValuesList = aisSeriesData.get(i);
			int numberOfMicroservices = adsValuesList.size();
			List<Integer> indexesToRemove = new ArrayList<Integer>();
			for (int j = numberOfMicroservices - 1; j >= 0; j--) {
				if ((Double) adsValuesList.get(j) == 0 && (Double) aisValuesList.get(j) == 0) {
					indexesToRemove.add(j);
				}
			}
			for (Integer index : indexesToRemove) {
				adsSeriesData.get(i).remove(index.intValue());
				aisSeriesData.get(i).remove(index.intValue());
			}
		}

	}

	/**
	 * @param adsSeriesData
	 */
	private GiniSeries<MetricType, Integer, BigDecimal> calculateGiniIndexBetweenMicroservices(List<List> adsSeriesData,
			MetricType metricType) {
		GiniSeries<MetricType, Integer, BigDecimal> giniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		giniSeries.setMetricType(metricType);
		HashMap<Integer, BigDecimal> giniHash = new HashMap<Integer, BigDecimal>();
		giniSeries.setSeriesData(giniHash);

		int i = 0;
		for (List list : adsSeriesData) {
			String valuesCommaSeparated = Arrays.toString(list.toArray()).replaceAll("\\[|\\]|", "");
			GiniCoefficientResult giniResult = restTemplate
					.getForObject(this.calculateGiniIndexURL + valuesCommaSeparated, GiniCoefficientResult.class);
			giniHash.put(i, giniResult.getGiniIndex());

			i += 1;
		}
		return giniSeries;
	}

	private int getSizeOfSeries(MicroservicesApplication microservicesApplication) {
		int sizeOfSeries = 0;
		if (microservicesApplication != null && microservicesApplication.getMicroservices() != null
				&& microservicesApplication.getMicroservices().size() > 0) {
			Microservice firstMicroservice = microservicesApplication.getMicroservices().get(0);
			if (firstMicroservice != null && firstMicroservice.getMetrics() != null
					&& firstMicroservice.getMetrics().size() > 0) {
				Metric metric = firstMicroservice.getMetrics().get(0);
				sizeOfSeries = metric.getReleases().length;
			}
		}
		return sizeOfSeries;
	}

	/**
	 * @param microservices
	 */
	private void calculateGiniForMicroservices(List<Microservice> microservices) {
		for (Microservice microservice : microservices) {
			calculateGiniValues(microservice);
		}
	}

	/**
	 * Calculate gini series only for ADS and AIS metrics
	 * 
	 * @param microservice
	 */
	private void calculateGiniValues(Microservice microservice) {

		Object[] adsMetricValues = getMetricValues(microservice, MetricType.ADS);
		Object[] aisMetricValues = getMetricValues(microservice, MetricType.AIS);

		// get the index that represents the release of creation for the microservice.
		// some microservices are included during the evolution and the 0s values
		// in the initial releases need to be removed to not impact the gini index
		// calculated
		int startIndex = getStartIndex(adsMetricValues, aisMetricValues);
		int originalSize = adsMetricValues.length;
		adsMetricValues = Arrays.copyOfRange(adsMetricValues, startIndex, originalSize);
		aisMetricValues = Arrays.copyOfRange(aisMetricValues, startIndex, originalSize);

		String values = Arrays.toString(adsMetricValues).replaceAll("\\[|\\]|", "");
		GiniCoefficientResult giniResult = restTemplate.getForObject(this.calculateGiniIndexURL + values,
				GiniCoefficientResult.class);

		GiniSeries<MetricType, Integer, BigDecimal> giniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		giniSeries.setMetricType(MetricType.ADS);
		HashMap<Integer, BigDecimal> giniHash = new HashMap<Integer, BigDecimal>();
		giniHash.put(0, giniResult.getGiniIndex());
		giniSeries.setSeriesData(giniHash);
		microservice.addGiniSeries(giniSeries);

		String aisValues = Arrays.toString(aisMetricValues).replaceAll("\\[|\\]|", "");
		GiniCoefficientResult aisGiniResult = restTemplate.getForObject(this.calculateGiniIndexURL + aisValues,
				GiniCoefficientResult.class);

		GiniSeries<MetricType, Integer, BigDecimal> aisGiniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		aisGiniSeries.setMetricType(MetricType.AIS);
		HashMap<Integer, BigDecimal> aisGiniHash = new HashMap<Integer, BigDecimal>();
		aisGiniHash.put(0, aisGiniResult.getGiniIndex());
		aisGiniSeries.setSeriesData(aisGiniHash);
		microservice.addGiniSeries(aisGiniSeries);

	}

	/**
	 * @param adsMetricValues
	 * @param aisMetricValues
	 * @return
	 */
	private int getStartIndex(Object[] adsMetricValues, Object[] aisMetricValues) {
		int startIndex = 0;
		for (int i = 0; i < adsMetricValues.length; i++) {
			// condition represents that microservice not exists yet
			if ((Double) adsMetricValues[i] == 0 && (Double) aisMetricValues[i] == 0) {
				startIndex += 1;
			} else {
				break;
			}
		}
		return startIndex;
	}

	private Object[] getMetricValues(Microservice microservice, MetricType metricType) {
		Object[] metricValues = null;
		Optional<Metric> metric = microservice.getMetrics().stream().filter(m -> m.getType().equals(metricType))
				.findFirst();
		if (metric.isPresent()) {
			metricValues = (Object[]) metric.get().getValues();
		}
		return metricValues;
	}

	/**
	 * @return
	 */
	private List<MicroservicesApplication> getAllMicroservicesApplication() {
		List<MicroservicesApplication> mcsAppList = new ArrayList<MicroservicesApplication>();
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:{**metrics.json}");

		List<Path> filesList = MicroservicesGraphUtil.findFiles(searchFolder, matcher);
		Gson gson = new GsonBuilder().create();
		for (Path path : filesList) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				MicroservicesApplication app = gson.fromJson(reader, MicroservicesApplication.class);
				mcsAppList.add(app);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mcsAppList;
	}
}
