/**
 * 
 */
package br.unicamp.ic.ginicoefficient.controller;

import java.math.BigDecimal;

import javax.persistence.Entity;

/**
 * @author Daniel R. F. Apolinario
 *
 */
@Entity
public class GiniCoefficient {

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
