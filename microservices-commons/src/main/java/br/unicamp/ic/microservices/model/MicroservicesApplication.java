/**
 * 
 */
package br.unicamp.ic.microservices.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.jgrapht.graph.DefaultEdge;

import com.google.gson.annotations.Expose;

import br.unicamp.ic.microservices.graphs.MicroservicesGraph;
import br.unicamp.ic.microservices.metrics.GiniSeries;
import br.unicamp.ic.microservices.metrics.Metric;
import br.unicamp.ic.microservices.metrics.Metric.MetricType;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class MicroservicesApplication implements Application {

	@Expose
	private String name;

	@Expose
	private List<MicroservicesGraph<String, DefaultEdge>> dependenciesGraphs;

	@Expose
	private List<Metric> metrics;

	@Expose
	private List<Microservice> microservices;

	@Expose
	private List<GiniSeries<MetricType, Integer, BigDecimal>> giniSeries;

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.unicamp.ic.microservices.graphs.Application#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see br.unicamp.ic.microservices.graphs.Application#setName()
	 */
	@Override
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the dependenciesGraph
	 */
	public List<MicroservicesGraph<String, DefaultEdge>> getDependenciesGraphs() {
		return dependenciesGraphs;
	}

	/**
	 * @param dependenciesGraph the dependenciesGraph to set
	 */
	public void setDependenciesGraphs(List<MicroservicesGraph<String, DefaultEdge>> dependenciesGraphs) {
		this.dependenciesGraphs = dependenciesGraphs;
	}

	public List<Metric> getMetrics() {
		return this.metrics;
	}

	public void setMetrics(List<Metric> metrics) {
		this.metrics = metrics;
	}

	public List<Microservice> getMicroservices() {
		return microservices;
	}

	public void setMicroservices(List<Microservice> microservices) {
		this.microservices = microservices;
	}

	public void addMicroservice(Microservice microservice) {
		if (this.microservices == null) {
			this.microservices = new ArrayList<Microservice>();
		}
		this.microservices.add(microservice);
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

	public void addGiniSeries(GiniSeries<MetricType, Integer, BigDecimal> giniSeries) {
		if (this.giniSeries == null) {
			this.giniSeries = new ArrayList<GiniSeries<MetricType, Integer, BigDecimal>>();
		}
		this.giniSeries.add(giniSeries);
	}

}
