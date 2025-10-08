package com.falkknudsen.osmunda;

import com.falkknudsen.jaywalk.*;
import com.falkknudsen.jaywalk.rtree.RTreeManager;
import com.falkknudsen.jaywalk.tstree.TSTManager;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Container class for data obtained in an OSM file that is passed along to the programme. */
public class LayeredMapData implements DataContainer, Serializable {
    public float minLat, minLon, maxLat, maxLon;
    public final IDrawableContainer drawables = new RTreeManager();
    public final TSTManager tstManager;

    /** Maps {@link Node}s to the IDs used by the directed graph internally.
     Only contains the "important" nodes, i.e. ones at start/end of a road, or which are referenced
     more than once. */
    public Map<Node, Integer> graphIDs = HashMap.newHashMap(11000);

    /** Ways with specific tags that mark them as being a road. */
    public List<HighWay> graphRoads;

    public LayeredMapData(String filename) {
        tstManager = new TSTManager(filename);
        tstManager.clearOldTSTs();
    }

    public void addRoad(HighWay road) {
        drawables.add(road);
    }

    public void insert(IDrawable item) {
        drawables.add(item);
    }

    @Override
    public String toString() {
        return "Bounds: " + Rectangle.toString(minLat, minLon, maxLat, maxLon)
                + "\nItems: " + drawables.size();
    }
}

