package app.core;

import org.apache.commons.configuration2.CombinedConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.tree.NodeCombiner;
import org.apache.commons.configuration2.tree.OverrideCombiner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class PropertiesOperators {
    public HashSet<String> propsOnlyInFirst(HashSet<String> hashLeft, HashSet<String> hashRight) {
        HashSet<String> resultHash = new HashSet<String>();
        resultHash = hashLeft;
        resultHash.removeAll(hashRight);
        return resultHash;
    }

    public List<String> DiffList(List<String> paramsLeft, List<String> paramsRight) {
        List<String> difference = new ArrayList<String>();
        difference = paramsLeft;
        difference.removeAll(paramsRight);
        return difference;
    }

    public CombinedConfiguration mergeProperties(Configuration oldConfig, Configuration newConfig) {
        NodeCombiner combiner = new OverrideCombiner();

        CombinedConfiguration combinedConfig = new CombinedConfiguration(combiner);
        combinedConfig.addConfiguration(oldConfig);
        combinedConfig.addConfiguration(newConfig);
        return combinedConfig;
    }

}
