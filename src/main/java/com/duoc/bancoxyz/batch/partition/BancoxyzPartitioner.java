package com.duoc.bancoxyz.batch.partition;

import java.util.LinkedHashMap;
import java.util.Map;

public class BancoxyzPartitioner {

    public Map<String, String> buildPartitions() {
        Map<String, String> partitions = new LinkedHashMap<>();
        String[] groups = {"cuentas", "intereses", "transacciones"};

        for (int index = 0; index < groups.length; index++) {
            partitions.put("partition" + index, groups[index]);
        }

        return partitions;
    }
}
