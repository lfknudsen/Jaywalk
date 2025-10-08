package com.falkknudsen.jaywalk.contracts;

import java.util.Map;

public interface IAddressContainer {
    void addAddress(StringBuilder sb, Map<String, String> tags,
                    float lat, float lon);
}
