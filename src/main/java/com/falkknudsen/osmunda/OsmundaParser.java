package com.falkknudsen.osmunda;

import com.falkknudsen.jaywalk.HighWay;
import com.falkknudsen.jaywalk.Node;
import com.falkknudsen.jaywalk.Relation;
import com.falkknudsen.jaywalk.Way;
import com.falkknudsen.jaywalk.tstree.TernarySearchTree;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.IOException;
import java.util.*;

import static com.falkknudsen.jaywalk.util.Log.VERBOSE;
import static com.falkknudsen.jaywalk.util.Log.log;

/** OSM parser. Can be reused. */
public class OsmundaParser extends AbstractBufferedParser {

    LayeredMapData map;

    public OsmundaParser(String filename) {
        super(filename);
    }

    public OsmundaParser() {
        super();
    }

    @Override
    public LayeredMapData parse(XMLStreamReader in) throws XMLStreamException, IOException {
        long before = System.currentTimeMillis();
        log("Starting parsing now.");

        // reset so user can re-use same OsmundaParser instance multiple times.
        map = new LayeredMapData(filename);
        int graphIndex = 0;

        final List<Node> nodesInWay = new ArrayList<>(2000); // the nodes in the way currently being read
        final List<Way> waysInRelation = new ArrayList<>(maxMemberCount);
        final List<Relation> relationsInRelation = new ArrayList<>(maxMemberCount);
        final Map<String, String> tags = HashMap.newHashMap(100); // tags of the item being read
        Map<Node, Integer> usage;

        Map<Long, Node> nodes =         HashMap.newHashMap(numOfNodes);
        Map<Long, Way> ways =           HashMap.newHashMap(numOfWays);
        Map<Long, HighWay> highways =   HashMap.newHashMap(numOfHighways);

        TernarySearchTree addresses = new TernarySearchTree();
        StringBuilder sb = new StringBuilder(100);

        in.nextTag(); // get <osm>
        // Assure it is an .OSM file.
        if (in.getEventType() != XMLStreamConstants.START_ELEMENT || !in.getLocalName().equals("osm")) {
            in.close();
            throw new IOException("Expected 'osm' element. This is not an OSM file.");
        }

        in.nextTag();

        // Skip past initial optional and irrelevant elements <note> and <meta>
        try {
            while (!(in.getEventType() == XMLStreamConstants.START_ELEMENT && in.getLocalName().equals("bounds"))) {
                in.next();
                in.nextTag();
            }
        } catch (NoSuchElementException e) {
            in.close();
            throw new NoSuchElementException("Did not find a 'bounds' element." +
                    " This is not a valid OSM file.");
        }

        // At this point, we are either at a bounds element, or there's been a NoSuchElement exception
        //  because no bounds element was found.

        // Parsing begins in earnest from here
        map.minLat = Float.parseFloat(in.getAttributeValue(null, "minlat"));
        map.maxLat = Float.parseFloat(in.getAttributeValue(null, "maxlat"));
        map.minLon = Float.parseFloat(in.getAttributeValue(null, "minlon"));
        map.maxLon = Float.parseFloat(in.getAttributeValue(null, "maxlon"));
        in.nextTag(); // get (potentially implicit) </bounds>
        in.nextTag(); // get first <node>

        while (in.getLocalName().equals("node")) {
            long osmID = Long.parseLong(in.getAttributeValue(null, "id"));
            float lat = Float.parseFloat(in.getAttributeValue(null, "lat"));
            float lon = Float.parseFloat(in.getAttributeValue(null, "lon"));

            in.nextTag(); // get either </node> or <tag>. If the latter, then it is a start element.
            while (in.getEventType() == XMLStreamConstants.START_ELEMENT) { // while <tag> and not </node>
                tags.put(in.getAttributeValue(null, "k"),
                         in.getAttributeValue(null, "v"));
                in.nextTag(); // get </tag>
                in.nextTag(); // get either </node> or <tag>
            }
            addresses.addAddress(sb, tags, lat, lon);
            nodes.put(osmID, new Node(lat, lon));
            tags.clear();

            in.nextTag(); // get <node> or <way>
        }

        log("Parsed " + nodes.size() + " nodes.");
        usage = HashMap.newHashMap(nodes.size());

        while (in.getLocalName().equals("way")) {
            long id = Long.parseLong(in.getAttributeValue(null, "id"));
            in.nextTag(); // get <nd>, <tag>, or </way>
            while (in.getEventType() == XMLStreamConstants.START_ELEMENT) { // while <nd> or <tag>
                if (in.getLocalName().equals("tag")) {
                    String k = in.getAttributeValue(null, "k");
                    String v = in.getAttributeValue(null, "v");
                    tags.put(k, v);
                } else { // is <nd>
                    final long nodeRef = Long.parseLong(in.getAttributeValue(null, "ref"));
                    Node n = nodes.get(nodeRef);
                    if (n != null) {
                        nodesInWay.add(n);
                        //usage.merge(new Node(n), 1, Integer::sum);
                    }
                }
                in.nextTag(); // get </nd> or </tag>
                in.nextTag(); // get <nd>, <tag>, or </way>
            }
            Way way = Way.create(nodesInWay, tags);
            if (way instanceof HighWay road) {
                highways.put(id, road);
                ways.put(id, road);
                Node extremity = road.getFirst();
                if (!map.graphIDs.containsKey(extremity)) {
                    map.graphIDs.put(extremity, graphIndex++);
                }
                for (int i = 1; i < road.size() - 1; i++) {
                    usage.merge(road.get(i), 1, Integer::sum);
                }
                extremity = road.getLast();
                if (!map.graphIDs.containsKey(extremity)) {
                    map.graphIDs.put(extremity, graphIndex++);
                }
            } else if (way != null) {
                ways.put(id, way);
            }
            nodesInWay.clear();
            tags.clear();

            in.nextTag(); // get <way> or <relation>
        }

        // Initialise the list of roads that will be the source of the graph.
        map.graphRoads = new ArrayList<>(highways.size());
        for (HighWay road : highways.values()) {
            map.graphRoads.add(road);
            map.addRoad(road);
            for (int i = 1; i <  road.size() - 1; i++) {
                Node n = road.get(i);
                if (usage.get(n) > 1 && !map.graphIDs.containsKey(n)) {
                    map.graphIDs.put(n, graphIndex);
                    graphIndex++;
                }
            }
        }

        usage = null;
        nodes = null;
        highways = null;
        Map<Long, Relation> relations = HashMap.newHashMap(36437);

        while (in.getLocalName().equals("relation")) {
            long id = Long.parseLong(in.getAttributeValue(null, "id"));
            in.nextTag(); // get <member>, <tag>, or </relation>
            while (in.getEventType() == XMLStreamConstants.START_ELEMENT) { // while <member> or <tag>
                String localName = in.getLocalName();
                if (localName.equals("tag")) {
                    String k = in.getAttributeValue(null, "k");
                    String v = in.getAttributeValue(null, "v");
                    if (!(k.equals("name") && v.equals("Øer i det Danske Øpas"))) {
                        tags.put(k, v);
                    }
                } else {
                    String type = in.getAttributeValue(null, "type");
                    if (type.equals("way")) {
                        String strRef = in.getAttributeValue(null, "ref");
                        long ref = Long.parseLong(strRef);
                        if (ways.containsKey(ref)) {
                            waysInRelation.add(ways.get(ref));
                        }
                        /* else if (highways.containsKey(ref)) {
                            waysInRelation.add(highways.get(ref));
                        }*/
                    } else if (type.equals("relation")) {
                        String strRef = in.getAttributeValue(null, "ref");
                        long ref = Long.parseLong(strRef);
                        Relation relation = relations.get(ref);
                        if (relation != null) {
                            relationsInRelation.add(relation);
                        }
                    }
                }
                in.nextTag(); // get </member> or </tag>
                in.nextTag(); // get <member>, <tag>, or </relation>
            }
            makeRelation(relations, waysInRelation, relationsInRelation, tags, id);
            waysInRelation.clear();
            relationsInRelation.clear();
            tags.clear();

            in.nextTag(); // get <relation>
        }
        in.close();

        // Finished parsing XML file.
        // Now performing some post-parsing "pre"-computation.

        log("Parsed " + relations.size() + " relations.");
        log("Parsed .osm file with OsmosisParser in "
                + (System.currentTimeMillis() - before) + " ms.");

        long beforeSaveTST = System.currentTimeMillis();
        log("Saving mixed TST as separate files.");
        map.tstManager.separateTSTs(addresses);
        log("Saved TST in " + (System.currentTimeMillis() - beforeSaveTST) + " ms.");

        fillRTrees(map, ways, relations);

        return map;
    }

    /** Create and add a new relation to the collection if it fulfills the requirements. */
    private static void makeRelation(Map<Long, Relation> relations,
                                     List<Way> wayMembers,
                                     List<Relation> relationMembers,
                                     Map<String, String> tags, long relationId) {
        if (!tags.containsKey("route")) {
            relations.put(relationId, Relation.create(wayMembers, relationMembers, tags));
        }
    }

    /** Inserts non-road ways and relations into the R-trees. */
    private static void fillRTrees(LayeredMapData map, Map<Long, Way> ways,
                                   Map<Long, Relation> relations) {
        if (!VERBOSE) {
            relations.values().forEach(map::insert);
                 ways.values().forEach(map::insert);
        } else {
            long RtreeStart = System.currentTimeMillis();
            int numberOfItems = ways.size() + relations.size();
            int itemsLoaded = 0;
            int iterator = 0;
            for (Relation relation : relations.values()) {
                map.insert(relation);
                itemsLoaded++;
                iterator++;
                if (iterator == 10000) {
                    System.out.println(itemsLoaded + "/" + numberOfItems);
                    iterator = 0;
                }
            }
            iterator = 0;
            for (Way way : ways.values()) {
                map.insert(way);
                itemsLoaded++;
                iterator++;
                if (iterator == 10000) {
                    System.out.println(itemsLoaded + "/" + numberOfItems);
                    iterator = 0;
                }
            }
            System.out.println("Finished inserting to RTree after: "
                    + (System.currentTimeMillis() - RtreeStart) + " ms.");
        }
    }
}