package com.falkknudsen.jaywalk;

import java.util.Map;

public interface IAddressContainer {
    void addAddress(StringBuilder sb, Map<String, String> tags,
                    float lat, float lon);
}
