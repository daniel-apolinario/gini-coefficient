/**
 * 
 */
package br.unicamp.ic.dependencygraphs.model;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class Node {

	private String id;
	private String nodeType;
	private String namespace;
	private String service;
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getNodeType() {
		return nodeType;
	}
	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
}
