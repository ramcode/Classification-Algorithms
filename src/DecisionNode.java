import java.util.List;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class DecisionNode {
    public String feature;
    public List<DecisionNode> children;
    public boolean isLeaf;
    public String leafLabel;

    public DecisionNode(String feature,boolean isLeaf){
        this.feature = feature;
        if(isLeaf){
            this.children = null;
        }
    }

}
