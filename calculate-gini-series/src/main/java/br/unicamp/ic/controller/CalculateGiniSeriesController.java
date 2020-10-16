/**
 * 
 */
package br.unicamp.ic.controller;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

	private HashMap<String, BigDecimal> giniIndexCache = null;

	@Autowired
	private Environment environment;

	private RestTemplate restTemplate;

	public CalculateGiniSeriesController() {
		this.restTemplate = new RestTemplate();
	}

	@PostMapping
	public ResponseEntity<String> calculateGiniCoefficient() {

		List<MicroservicesApplication> msaList = new ArrayList<MicroservicesApplication>();

		msaList = calculateGiniSeriesData();

		exportTestResultsToJson(msaList);

		return ResponseEntity.status(HttpStatus.CREATED).build();
	}

	/**
	 * @param msaList
	 */
	private void exportTestResultsToJson(List<MicroservicesApplication> msaList) {
		for (MicroservicesApplication app : msaList) {

			try (FileOutputStream fos = new FileOutputStream(searchFolder + app.getName() + "/metrics-with-gini.json");
					OutputStreamWriter isr = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
				Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

				gson.toJson(app, isr);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

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
		List<List> adsFilteredSeriesData = new ArrayList<>();
		List<List> aisFilteredSeriesData = new ArrayList<>();
		List<List> siySeriesData = new ArrayList<>();
		for (int i = 0; i < sizeOfSeries; i++) {
			List<Object> adsMetricValues = new ArrayList<Object>();
			adsSeriesData.add(adsMetricValues);
			List<Object> aisMetricValues = new ArrayList<Object>();
			aisSeriesData.add(aisMetricValues);
			List<Object> adsFilteredMetricValues = new ArrayList<Object>();
			adsFilteredSeriesData.add(adsFilteredMetricValues);
			List<Object> aisFilteredMetricValues = new ArrayList<Object>();
			aisFilteredSeriesData.add(aisFilteredMetricValues);
		}
		int sizeOfReleases = msApp.getMicroservices().get(0).getMetrics().get(0).getReleases().length;
		for (int i = 0; i < sizeOfReleases; i++) {
			List<Object> siyMetricValues = new ArrayList<Object>();
			siySeriesData.add(siyMetricValues);
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
			Object[] adsFilteredMetricValues = getMetricValues(microservice, MetricType.ADS_FILTERED);
			if (adsFilteredMetricValues != null && adsFilteredMetricValues.length > 0) {
				int k = 0;
				for (Object value : adsFilteredMetricValues) {
					List metricValuesList = adsFilteredSeriesData.get(k);
					if (value != null) {
						metricValuesList.add(value);
					}
					k += 1;
				}
			}
			Object[] aisFilteredMetricValues = getMetricValues(microservice, MetricType.AIS_FILTERED);
			if (aisFilteredMetricValues != null && aisFilteredMetricValues.length > 0) {
				int l = 0;
				for (Object value : aisFilteredMetricValues) {
					List metricValuesList = aisFilteredSeriesData.get(l);
					if (value != null) {
						metricValuesList.add(value);
					}
					l += 1;
				}
			}

		}

		Object[] siyMetricValues = getMetricValues(msApp, MetricType.SIY);
		int i = 0;
		if (siyMetricValues != null && siyMetricValues.length > 0) {
			for (Object value : siyMetricValues) {
				List metricValuesList = siySeriesData.get(i);
				metricValuesList.add(value);
				i += 1;
			}
		}

		// identify invalid values when microservices were not created yet and
		// their metric values are zero.
		removeInvalidMetricValues(adsSeriesData, aisSeriesData, adsFilteredSeriesData, aisFilteredSeriesData);

		GiniSeries adsGiniSeries = calculateGiniIndexBetweenMicroservices(adsSeriesData, MetricType.ADS);
		GiniSeries aisGiniSeries = calculateGiniIndexBetweenMicroservices(aisSeriesData, MetricType.AIS);
		GiniSeries adsFilteredGiniSeries = calculateGiniIndexBetweenMicroservices(adsFilteredSeriesData,
				MetricType.ADS_FILTERED);
		GiniSeries aisFilteredGiniSeries = calculateGiniIndexBetweenMicroservices(aisFilteredSeriesData,
				MetricType.AIS_FILTERED);

		msApp.addGiniSeries(adsGiniSeries);
		msApp.addGiniSeries(aisGiniSeries);
		msApp.addGiniSeries(adsFilteredGiniSeries);
		msApp.addGiniSeries(aisFilteredGiniSeries);

		if (siyMetricValues != null && siyMetricValues.length > 0) {
			GiniSeries siyGiniSeries = calculateGiniIndexBetweenMicroservices(siySeriesData, MetricType.SIY);
			msApp.addGiniSeries(siyGiniSeries);
		}
	}

	/**
	 * @param adsSeriesData
	 * @param aisSeriesData
	 */
	private void removeInvalidMetricValues(List<List> adsSeriesData, List<List> aisSeriesData,
			List<List> adsFilteredSeriesData, List<List> aisFilteredSeriesData) {
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
				// adsFilteredSeriesData.get(i).remove(index.intValue());
				// aisFilteredSeriesData.get(i).remove(index.intValue());
			}
		}

	}

	/**
	 * @param adsSeriesData
	 */
	private GiniSeries<MetricType, Integer, BigDecimal> calculateGiniIndexBetweenMicroservices(List<List> seriesData,
			MetricType metricType) {
		GiniSeries<MetricType, Integer, BigDecimal> giniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		giniSeries.setMetricType(metricType);
		HashMap<Integer, BigDecimal> giniHash = new HashMap<Integer, BigDecimal>();
		giniSeries.setSeriesData(giniHash);

		int i = 0;
		for (List list : seriesData) {
			if (list != null && !list.isEmpty()) {
				String valuesCommaSeparated = Arrays.toString(list.toArray()).replaceAll("\\[|\\]|", "");
				System.out.println("api parameter = " + valuesCommaSeparated);
				GiniCoefficientResult giniResult = restTemplate
						.getForObject(this.calculateGiniIndexURL + valuesCommaSeparated, GiniCoefficientResult.class);
				giniHash.put(i, giniResult.getGiniIndex());

				i += 1;
			}
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
		Object[] adsFilteredMetricValues = getMetricValues(microservice, MetricType.ADS_FILTERED);
		Object[] aisFilteredMetricValues = getMetricValues(microservice, MetricType.AIS_FILTERED);

		// get the index that represents the release of creation for the microservice.
		// some microservices are included during the evolution and the 0s values
		// in the initial releases need to be removed to not impact the gini index
		// calculated
		int startIndex = getStartIndex(adsMetricValues, aisMetricValues);
		int originalSize = adsMetricValues.length;
		// adsMetricValues = Arrays.copyOfRange(adsMetricValues, startIndex,
		// originalSize);
		// aisMetricValues = Arrays.copyOfRange(aisMetricValues, startIndex,
		// originalSize);

		GiniSeries<MetricType, Integer, BigDecimal> adsGiniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		adsGiniSeries.setMetricType(MetricType.ADS);
		HashMap<Integer, BigDecimal> adsGiniHash = new HashMap<Integer, BigDecimal>();

		GiniSeries<MetricType, Integer, BigDecimal> aisGiniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		aisGiniSeries.setMetricType(MetricType.AIS);
		HashMap<Integer, BigDecimal> aisGiniHash = new HashMap<Integer, BigDecimal>();

		GiniSeries<MetricType, Integer, BigDecimal> adsFilteredGiniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		adsFilteredGiniSeries.setMetricType(MetricType.ADS_FILTERED);
		HashMap<Integer, BigDecimal> adsFilteredGiniHash = new HashMap<Integer, BigDecimal>();

		GiniSeries<MetricType, Integer, BigDecimal> aisFilteredGiniSeries = new GiniSeries<MetricType, Integer, BigDecimal>();
		aisFilteredGiniSeries.setMetricType(MetricType.AIS_FILTERED);
		HashMap<Integer, BigDecimal> aisFilteredGiniHash = new HashMap<Integer, BigDecimal>();

		for (int i = 0; i < originalSize; i++) {
			if (i >= startIndex) {
				Object[] adsMetricValuesRange = Arrays.copyOfRange(adsMetricValues, startIndex, i + 1);
				adsGiniHash.put(i, calculateGiniIndex(adsMetricValuesRange));

				Object[] aisMetricValuesRange = Arrays.copyOfRange(aisMetricValues, startIndex, i + 1);
				aisGiniHash.put(i, calculateGiniIndex(aisMetricValuesRange));

				if (adsFilteredMetricValues != null && adsFilteredMetricValues.length > 0) {
					Object[] adsFilteredMetricValuesRange = Arrays.copyOfRange(adsFilteredMetricValues, startIndex,
							i + 1);
					adsFilteredGiniHash.put(i, calculateGiniIndex(adsFilteredMetricValuesRange));
				} else {
					adsFilteredGiniHash.put(i, null);
				}

				if (aisFilteredMetricValues != null && aisFilteredMetricValues.length > 0) {
					Object[] aisFilteredMetricValuesRange = Arrays.copyOfRange(aisFilteredMetricValues, startIndex,
							i + 1);
					aisFilteredGiniHash.put(i, calculateGiniIndex(aisFilteredMetricValuesRange));
				} else {
					aisFilteredGiniHash.put(i, null);
				}

			} else {
				adsGiniHash.put(i, null);
				aisGiniHash.put(i, null);
				adsFilteredGiniHash.put(i, null);
				aisFilteredGiniHash.put(i, null);
			}

		}
		adsGiniSeries.setSeriesData(adsGiniHash);
		microservice.addGiniSeries(adsGiniSeries);

		aisGiniSeries.setSeriesData(aisGiniHash);
		microservice.addGiniSeries(aisGiniSeries);

		adsFilteredGiniSeries.setSeriesData(adsFilteredGiniHash);
		microservice.addGiniSeries(adsFilteredGiniSeries);

		aisFilteredGiniSeries.setSeriesData(aisFilteredGiniHash);
		microservice.addGiniSeries(aisFilteredGiniSeries);

	}

	private BigDecimal calculateGiniIndex(Object[] metricValuesRange) {
		String valuesRange = Arrays.toString(metricValuesRange).replaceAll("\\[|\\]|", "");
		BigDecimal giniResultCache = getGiniIndexCache().get(valuesRange);
		// if not foun in cache, to calculate by API
		if (giniResultCache == null) {
			GiniCoefficientResult giniResult = restTemplate.getForObject(this.calculateGiniIndexURL + valuesRange,
					GiniCoefficientResult.class);
			giniResultCache = giniResult.getGiniIndex();
			getGiniIndexCache().put(valuesRange, giniResultCache);
		} else {
			System.out.println("Result found in cache!");
		}
		return giniResultCache;

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

	private Object[] getMetricValues(MicroservicesApplication microservicesApplication, MetricType metricType) {
		Object[] metricValues = null;
		Optional<Metric> metric = microservicesApplication.getMetrics().stream()
				.filter(m -> m.getType().equals(metricType)).findFirst();
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

	/**
	 * @return the giniIndexCache
	 */
	public HashMap<String, BigDecimal> getGiniIndexCache() {
		if (this.giniIndexCache == null) {
			this.giniIndexCache = new HashMap<String, BigDecimal>();
		}
		return giniIndexCache;
	}

	/**
	 * @param giniIndexCache the giniIndexCache to set
	 */
	public void setGiniIndexCache(HashMap<String, BigDecimal> giniIndexCache) {
		this.giniIndexCache = giniIndexCache;
	}
}
