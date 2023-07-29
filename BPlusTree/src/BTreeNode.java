class BTreeNode {

    /**
     * Array of the keys stored in the node.
     */
    long[] keys;
    /**
     * Array of the values[recordID] stored in the node. This will only be filled when the node is a leaf node.
     */
    long[] values;
    /**
     * Minimum degree (defines the range for number of keys)
     **/
    int t;
    /**
     * Pointers to the children, if this node is not a leaf.  If
     * this node is a leaf, then null.
     */
    BTreeNode[] children;
    /**
     * number of key-value pairs in the B-tree
     */
    int n;
    /**
     * true when node is leaf. Otherwise false
     */
    boolean leaf;

    /**
     * point to other next node when it is a leaf node. Otherwise null
     */
    BTreeNode next;

    // Constructor
    BTreeNode(int t, boolean leaf) {
        this.t = t;
        this.leaf = leaf;
//        this.keys = new long[2 * t - 1];
        this.keys = new long[2 * t];
//        this.children = new BTreeNode[2 * t];
        this.children = new BTreeNode[2 * t + 1];
        this.n = 0;
        this.next = null;
//        this.values = new long[2 * t - 1];
        this.values = new long[2 * t];
    }
    
    void nodeDebugPrint(boolean integrityCheck) {
    	nodeDebugPrint(integrityCheck, false);
    }
    
    void nodeDebugPrint(boolean integrityCheck, boolean showValues) {
    	int nonZeroCnt = 0;
    	System.out.print("[");
    	for (int i = 0; i < keys.length; i++) {
    		if (showValues) {
    			System.out.print("(" + keys[i] +"," + values[i] + ")");
    		} else {
    			System.out.print("(" + keys[i] + ")");
    		}
    		
    		if (keys[i] > 0) { nonZeroCnt++; }
    	}
    	String nOut = this.n != nonZeroCnt ? "!" : "";
    	System.out.print(" (n=" + this.n + nOut + ")] ");
    }
    
    int numChildren() {
    	int numChildren = 0;
    	for (int j = 0 ;j < this.children.length; j++) {
    		if (children[j] != null) {
    			numChildren += 1;
    		}
    	}
    	return numChildren;
    }
    
    int numKeys() {
    	int numKeys = 0;
    	for (int j = 0 ;j < this.keys.length; j++) {
    		if (keys[j] != 0) {
    			numKeys += 1;
    		}
    	}
    	return numKeys;
    }
}
