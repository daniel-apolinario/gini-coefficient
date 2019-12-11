/**
 * 
 */
package br.unicamp.ic.microservices.experiment;

import java.util.List;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class ExperimentDesignConfig {

	public enum GraphStructure {
		RANDOM_GRAPH, BARABASI_ALBERT_GRAPH
	}

	public enum GraphSize {
		SMALL, MEDIUM, BIG
	}

	public enum GraphScenario {
		IMPROVE, WORSEN
	}

	private int replicasQuantity;

	private List<ExperimentDesignConfig.GraphStructure> graphStructureFactor;

	private List<ExperimentDesignConfig.GraphSize> graphSizeFactor;

	private List<ExperimentDesignConfig.GraphScenario> graphScenarioFactor;

	public int getReplicasQuantity() {
		return replicasQuantity;
	}

	public void setReplicasQuantity(int replicasQuantity) {
		this.replicasQuantity = replicasQuantity;
	}

	public List getGraphStructureFactor() {
		return graphStructureFactor;
	}

	public void setGraphStructureFactor(List graphStructureFactor) {
		this.graphStructureFactor = graphStructureFactor;
	}

	public List getGraphSizeFactor() {
		return graphSizeFactor;
	}

	public void setGraphSizeFactor(List graphSizeFactor) {
		this.graphSizeFactor = graphSizeFactor;
	}

	public List getGraphScenarioFactor() {
		return graphScenarioFactor;
	}

	public void setGraphScenarioFactor(List graphScenarioFactor) {
		this.graphScenarioFactor = graphScenarioFactor;
	}


}
