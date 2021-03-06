/**
 * 
 */
package br.unicamp.ic.microservices.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

import br.unicamp.ic.microservices.metrics.GiniSeries;
import br.unicamp.ic.microservices.metrics.Metric;
import br.unicamp.ic.microservices.metrics.Metric.MetricType;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class Microservice {

	@Expose
	private String name;

	private Application application;

	@Expose
	private List<Metric> metrics;

	@Expose
	private List<GiniSeries<MetricType, Integer, BigDecimal>> giniSeries;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the application
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * @return the metrics
	 */
	public List<Metric> getMetrics() {
		return metrics;
	}

	/**
	 * @param metrics the metrics to set
	 */
	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public void addMetric(Metric metric) {
		if (this.metrics == null) {
			this.metrics = new ArrayList<Metric>();
		}
		this.metrics.add(metric);
	}

	public List<GiniSeries<MetricType, Integer, BigDecimal>> getGiniSeries() {
		return giniSeries;
	}

	public void setGiniSeries(List<GiniSeries<MetricType, Integer, BigDecimal>> giniSeries) {
		this.giniSeries = giniSeries;
	}

	public void addGiniSeries(GiniSeries<MetricType, Integer, BigDecimal> giniSeriesElement) {
		if (this.giniSeries == null) {
			this.giniSeries = new ArrayList<GiniSeries<MetricType, Integer, BigDecimal>>();
		}
		this.giniSeries.add(giniSeriesElement);
	}

}
