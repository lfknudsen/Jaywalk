package com.falkknudsen.jaywalk;

import java.io.Serializable;
import java.util.List;

public class HighWay extends Way implements Serializable {
    private HighWay(List<Node> nodes) {
        super(nodes);
    }
}
