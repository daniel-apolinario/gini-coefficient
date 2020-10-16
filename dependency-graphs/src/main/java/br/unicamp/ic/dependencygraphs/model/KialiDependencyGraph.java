/**
 * 
 */
package br.unicamp.ic.dependencygraphs.model;

import java.util.List;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class KialiDependencyGraph {

	private String originalVersion;
	private String releaseOrder;
	private List<Node> nodes;
	private List<Edge> edges;

	public String getOriginalVersion() {
		return originalVersion;
	}

	public void setOriginalVersion(String originalVersion) {
		this.originalVersion = originalVersion;
	}

	public String getReleaseOrder() {
		return releaseOrder;
	}

	public void setReleaseOrder(String releaseOrder) {
		this.releaseOrder = releaseOrder;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}

}
