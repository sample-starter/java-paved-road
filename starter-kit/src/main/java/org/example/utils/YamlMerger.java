package org.example.utils;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;


public class YamlMerger {

    public static Map<String, Object> loadYaml(String filePath) throws IOException {
        try (FileInputStream inputStream = new FileInputStream(filePath)) {
            Yaml yaml = new Yaml(new Constructor(Map.class));
            return yaml.load(inputStream);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> mergeYamlData(Map<String, Object> yamlData1, Map<String, Object> yamlData2) {
        for (String key : yamlData2.keySet()) {
            Object value1 = yamlData1.get(key);
            Object value2 = yamlData2.get(key);

            if (value1 instanceof Map && value2 instanceof Map) {
                yamlData1.put(key, mergeYamlData((Map<String, Object>) value1, (Map<String, Object>) value2));
            } else {
                yamlData1.put(key, value2);
            }
        }
        return yamlData1;
    }

    public static void saveYaml(Map<String, Object> yamlData, Path filePath) throws IOException {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);

        try (FileWriter writer = new FileWriter(filePath.toFile())) {
            yaml.dump(yamlData, writer);
        }
    }

}
