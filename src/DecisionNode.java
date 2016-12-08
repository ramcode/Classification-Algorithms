import java.util.List;

/**
 * Created by VenkataRamesh on 12/7/2016.
 */
public class DecisionNode {
    public String feature;
    public DecisionNode left;
    public DecisionNode right;
    public boolean isLeaf;
    public String leafLabel;
    public static final String CLASS_LABEL_NO = "CLASS_0";
    public static final String CLASS_LABEL_YES = "CLASS_1";
    public static final String NODE_LABEL = "ATTRIBUTE";
    public String leftEdgeLabel;
    public String rightEdgeLabel;

    public DecisionNode(String feature, boolean isLeaf) {
        if (isLeaf) {
            this.feature = feature.equals("0") ? CLASS_LABEL_NO : CLASS_LABEL_YES;
        } else {
            this.feature = NODE_LABEL + "_" + feature + 1;
        }
    }

    public void setLeftChild(DecisionNode node) {
        this.left = node;
    }

    public void setLeftEdgeLabel(String leftLabel) {
        this.leftEdgeLabel = feature + " <= " + leftLabel;
    }

    public void setRightEdgeLabel(String rightEdgeLabel) {
        this.rightEdgeLabel = feature + " > " + rightEdgeLabel;
    }

    public void setRightChild(DecisionNode node) {
        this.right = node;
    }


}
