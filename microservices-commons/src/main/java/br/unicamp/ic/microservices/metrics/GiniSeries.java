/**
 * 
 */
package br.unicamp.ic.microservices.metrics;

import java.util.HashMap;

import com.google.gson.annotations.Expose;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class GiniSeries<T, K, V> {

	@Expose
	private T metricType;
	
	@Expose
	private HashMap<K, V> seriesData;

	public T getMetricType() {
		return metricType;
	}

	public void setMetricType(T metricType) {
		this.metricType = metricType;
	}

	public HashMap<K, V> getSeriesData() {
		return seriesData;
	}

	public void setSeriesData(HashMap<K, V> seriesData) {
		this.seriesData = seriesData;
	}
}
