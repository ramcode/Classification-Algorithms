import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class DecisionNode {
    public String featureLabel;
    public Map<String, DecisionNode> children = new HashMap<>();
    public boolean isLeaf;
    public int attributeIndex;
    public double splitAttributeCutValue;

    public DecisionNode(String featureLabel) {
        this.featureLabel = featureLabel;
    }

    public DecisionNode() {

    }

    public void setLeaf(boolean isLeaf) {
        this.isLeaf = isLeaf;
    }

    public void setFeatureLabel(String feature) {
        this.featureLabel = feature;
    }

    public void addNode(DecisionNode node, String edgeLabel) {
        this.children.put(edgeLabel, node);
    }
}
