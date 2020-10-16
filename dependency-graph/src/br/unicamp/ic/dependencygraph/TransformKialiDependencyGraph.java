/**
 * 
 */
package br.unicamp.ic.dependencygraph;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import br.unicamp.ic.dependencygraph.model.Edge;
import br.unicamp.ic.dependencygraph.model.KialiDependencyGraph;
import br.unicamp.ic.dependencygraph.model.Node;
import br.unicamp.ic.microservices.graphs.MicroservicesGraph;
import br.unicamp.ic.microservices.graphs.MicroservicesGraphUtil;

/**
 * @author Daniel R. F. Apolinario
 *
 */
public class TransformKialiDependencyGraph {

	private static final String searchFolder = "/home/daniel/Documentos/mestrado-2018/projeto/spinnaker-kiali";
	private static final String jsonFilesPrefix = "service-graph-version-";
	private static final String NAMESPACE_DEFAULT = "spinnaker";
	private static final String exportFolder = "/home/daniel/Documentos/mestrado-2018/projeto/grafos-dependencia/application-0001";
	private static final String prefixOutputFile = "release-";
	private static final String prefixKialiServiceName = "spin-";
	private static final String initialReleaseKayenta = "1.7.0";
	private static final String ORCA_SERVICE_NAME = "orca";
	private static final String KAYENTA_SERVICE_NAME = "kayenta";
	private static final String REDIS_SERVICE_NAME = "redis";
	private static final String DECK_SERVICE_NAME = "deck";
	private static final String SAMPLEAPP_SERVICE_NAME = "sampleapp";

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		List<KialiDependencyGraph> kialiDependenciesGraph = readJsonFiles();
		cleanKialiDependenciesGraph(kialiDependenciesGraph);
		List<MicroservicesGraph<String, DefaultEdge>> applicationGraphs = generateApplicationGraphs(
				kialiDependenciesGraph);
		exportApplicationGraphsToDOTFile(applicationGraphs);
	}

	/**
	 * This method support add specific edges to the nodes
	 * 
	 * @param kialiDependenciesGraph
	 */
	private static void applyAdjusts(KialiDependencyGraph kialiDependencyGraph,
			MicroservicesGraph<String, DefaultEdge> graph) {
		if (kialiDependencyGraph != null) {
			if (releaseIsGreaterThan(kialiDependencyGraph.getOriginalVersion(), initialReleaseKayenta)) {
				graph.addEdge(ORCA_SERVICE_NAME, KAYENTA_SERVICE_NAME);
			}
		}
	}

	/**
	 * @param originalVersion
	 * @param initialreleasekayenta2
	 * @return
	 */
	private static boolean releaseIsGreaterThan(String originalVersion, String initialreleasekayenta) {
		String[] originalVersionNumbers = originalVersion.split("\\.");
		String[] initialReleaseKayentaNumbers = initialreleasekayenta.split("\\.");
		int originalVersionMajorRelease = Integer.parseInt(originalVersionNumbers[0]);
		int initialReleaseKayentaMajorRelease = Integer.parseInt(initialReleaseKayentaNumbers[0]);
		if (originalVersionMajorRelease > initialReleaseKayentaMajorRelease) {
			return true;
		} else {
			if (originalVersionMajorRelease == initialReleaseKayentaMajorRelease) {
				int originalVersionMinorRelease = Integer.parseInt(originalVersionNumbers[1]);
				int initialReleaseKayentaMinorRelease = Integer.parseInt(initialReleaseKayentaNumbers[1]);
				if (originalVersionMinorRelease > initialReleaseKayentaMinorRelease) {
					return true;
				} else {
					if (originalVersionMinorRelease == initialReleaseKayentaMinorRelease) {
						int originalVersionPatch = Integer.parseInt(originalVersionNumbers[2]);
						int initialReleaseKayentaPatch = Integer.parseInt(initialReleaseKayentaNumbers[2]);
						if (originalVersionPatch > initialReleaseKayentaPatch) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * @param applicationGraphs
	 */
	private static void exportApplicationGraphsToDOTFile(
			List<MicroservicesGraph<String, DefaultEdge>> applicationGraphs) {
		if (applicationGraphs != null && !applicationGraphs.isEmpty()) {
			for (MicroservicesGraph<String, DefaultEdge> graph : applicationGraphs) {
				MicroservicesGraphUtil.exportGraphToFile(graph, exportFolder, graph.getFileName());
			}
		}
	}

	/**
	 * @param kialiDependenciesGraph
	 */
	private static List<MicroservicesGraph<String, DefaultEdge>> generateApplicationGraphs(
			List<KialiDependencyGraph> kialiDependenciesGraph) {
		List<MicroservicesGraph<String, DefaultEdge>> applicationGraphs = null;
		if (kialiDependenciesGraph != null && kialiDependenciesGraph.size() > 0) {
			applicationGraphs = new ArrayList<MicroservicesGraph<String, DefaultEdge>>();
			for (KialiDependencyGraph kialiDependencyGraph : kialiDependenciesGraph) {
				Map<String, String> nodesMap = createNodesMap(kialiDependencyGraph.getNodes());

				MicroservicesGraph<String, DefaultEdge> graph = new MicroservicesGraph(DefaultEdge.class);
				for (Node node : kialiDependencyGraph.getNodes()) {
					graph.addVertex(node.getService());
				}
				for (Edge edge : kialiDependencyGraph.getEdges()) {
					graph.addEdge(nodesMap.get(edge.getSource()), nodesMap.get(edge.getTarget()));
				}
				applyAdjusts(kialiDependencyGraph, graph);
				graph.setFileName(prefixOutputFile + kialiDependencyGraph.getReleaseOrder());
				applicationGraphs.add(graph);
			}

		}
		return applicationGraphs;
	}

	/**
	 * @param nodes
	 * @return
	 */
	private static Map<String, String> createNodesMap(List<Node> nodes) {
		Map<String, String> mapNodes = null;
		if (nodes != null && !nodes.isEmpty()) {
			mapNodes = new HashMap<String, String>();
			for (Node node : nodes) {
				mapNodes.put(node.getId(), node.getService());
			}
		}
		return mapNodes;
	}

	/**
	 * @param kialiDependenciesGraph
	 */
	private static void cleanKialiDependenciesGraph(List<KialiDependencyGraph> kialiDependenciesGraph) {
		if (kialiDependenciesGraph != null && !kialiDependenciesGraph.isEmpty()) {
			for (KialiDependencyGraph kialiDependencyGraph : kialiDependenciesGraph) {
				Map<String, Node> mapNodesToRemove = new HashMap<String, Node>();
				for (Node node : kialiDependencyGraph.getNodes()) {
					if (!NAMESPACE_DEFAULT.equals(node.getNamespace()) || node.getService() == null
							|| node.getService().isEmpty() || REDIS_SERVICE_NAME.equals(node.getService())
							|| DECK_SERVICE_NAME.equals(node.getService())) {
						mapNodesToRemove.put(node.getId(), node);
					}
				}
				if (mapNodesToRemove.size() > 0) {
					kialiDependencyGraph.getNodes().removeAll(mapNodesToRemove.values());
					List<Edge> edgesToRemove = new ArrayList<Edge>();
					for (Edge edge : kialiDependencyGraph.getEdges()) {
						if (mapNodesToRemove.containsKey(edge.getSource())
								|| mapNodesToRemove.containsKey(edge.getTarget())) {
							edgesToRemove.add(edge);
						}
					}
					if (edgesToRemove.size() > 0) {
						kialiDependencyGraph.getEdges().removeAll(edgesToRemove);
					}
				}
			}

		}
	}

	/**
	 * @return
	 */
	private static List<KialiDependencyGraph> readJsonFiles() {

		List<KialiDependencyGraph> kialiDependenciesGraph = null;
		PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:{**.json}");
		List<Path> filesList = MicroservicesGraphUtil.findFiles(searchFolder, matcher);
		if (filesList != null && filesList.size() > 0) {
			kialiDependenciesGraph = new ArrayList<KialiDependencyGraph>();
			Gson gson = new GsonBuilder().create();
			int count = 0;
			for (Path path : filesList) {
				try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
					KialiDependencyGraph kialiDependencyGraph = new KialiDependencyGraph();
					kialiDependencyGraph.setOriginalVersion(path.getFileName().toString()
							.substring(jsonFilesPrefix.length(), path.getFileName().toString().length() - 5));

					JsonObject convertedObject = gson.fromJson(reader, JsonObject.class);
					JsonObject elements = (JsonObject) convertedObject.get("elements");
					JsonArray nodes = (JsonArray) elements.get("nodes");
					List<Node> nodeList = getNodesFromKialiDependencyGraph(nodes);
					kialiDependencyGraph.setNodes(nodeList);
					JsonArray edges = (JsonArray) elements.get("edges");
					List<Edge> edgeList = getEdgesFromKialiDependencyGraph(edges);
					kialiDependencyGraph.setEdges(edgeList);
					kialiDependencyGraph.setReleaseOrder(String.format("%02d", count));
					kialiDependenciesGraph.add(kialiDependencyGraph);
					count += 1;
				} catch (Exception e) {
					System.out.println("exception=" + e.getStackTrace());
					e.printStackTrace();
				}
			}
		}
		return kialiDependenciesGraph;
	}

	/**
	 * @param edges
	 * @return
	 */
	private static List<Edge> getEdgesFromKialiDependencyGraph(JsonArray edges) {
		List<Edge> edgeList = null;
		if (edges != null && edges.size() > 0) {
			edgeList = new ArrayList<Edge>();
			for (JsonElement nodeElement : edges) {
				JsonElement dataElement = nodeElement.getAsJsonObject().get("data");
				Edge edge = new Edge(dataElement.getAsJsonObject().get("source").getAsString(),
						dataElement.getAsJsonObject().get("target").getAsString());
				edgeList.add(edge);
			}
		}
		return edgeList;
	}

	/**
	 * @param nodes
	 * @return
	 */
	private static List<Node> getNodesFromKialiDependencyGraph(JsonArray nodes) {
		List<Node> nodeList = null;
		if (nodes != null && nodes.size() > 0) {
			nodeList = new ArrayList<Node>();
			for (JsonElement nodeElement : nodes) {
				JsonElement dataElement = nodeElement.getAsJsonObject().get("data");
				Node node = new Node();
				node.setId(dataElement.getAsJsonObject().get("id").getAsString());
				node.setNamespace(dataElement.getAsJsonObject().get("namespace").getAsString());
				node.setNodeType(dataElement.getAsJsonObject().get("nodeType").getAsString());
				if (dataElement.getAsJsonObject().get("service") != null
						&& !dataElement.getAsJsonObject().get("service").getAsString().equals(SAMPLEAPP_SERVICE_NAME)) {
					node.setService(dataElement.getAsJsonObject().get("service").getAsString()
							.substring(prefixKialiServiceName.length()));
				}
				nodeList.add(node);
			}
		}
		return nodeList;
	}

}
