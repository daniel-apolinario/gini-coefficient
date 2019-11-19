/**
 * 
 */
package br.unicamp.ic.microservices.metrics;

import java.math.BigDecimal;

import javax.persistence.Entity;

/**
 * @author Daniel R. F. Apolinario
 *
 */
@Entity
public class GiniCoefficientResult {

	private BigDecimal giniIndex;
	
	private int port;

	public BigDecimal getGiniIndex() {
		return giniIndex;
	}

	public void setGiniIndex(BigDecimal giniIndex) {
		this.giniIndex = giniIndex;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}
