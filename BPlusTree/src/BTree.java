import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * B+Tree Structure
 * Key - StudentId
 * Leaf Node should contain [ key,recordId ]
 */
class BTree {

    /**
     * Pointer to the root node.
     */
    private BTreeNode root;
    /**
     * Number of key-value pairs allowed in the tree/the minimum degree of B+Tree
     **/
    private int t;

    BTree(int t) {
        this.root = null;
        this.t = t;
    }

    long search(long studentId) {
        /**
         * TODO:
         * Implement this function to search in the B+Tree.
         * Return recordID for the given StudentID.
         * Otherwise, print out a message that the given studentId has not been found in the table and return -1.
         */
        BTreeNode leafNode = treeSearch(root, studentId);
        
        for (int i = 0; i < leafNode.keys.length; i++) {
        	if (leafNode.keys[i] == studentId) {
        		return leafNode.values[i];
        	}
        }

        return -1;
    }

    public BTreeNode treeSearch(BTreeNode parent, long key) {
        // Base - parent is leaf
        if(parent.leaf) {
            return parent;
        }

        for (int i = 0; i < parent.n; i++) {
            long nodeKey = parent.keys[i];
            if (nodeKey < key) {
                return treeSearch(parent.children[i], key);
            }

            if (i == (parent.keys.length - 1)) {
                return treeSearch(parent.children[i+1], key);
            }
        }

        return null;
    }

    BTree insert(Student student) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */
    	
    	// If there is no root node, create one
    	if (this.root == null) {
    		this.root = new BTreeNode(this.t, true);
    	}
    	
    	// Get the student id and record id and insert recursively
    	long studentId = student.studentId;
    	long recordId = student.recordId;
    	BTreeEntry studentEntry = new BTreeEntry(student.studentId, student.recordId);
    	BTreeEntry newEntry = new BTreeEntry();
    	
    	this.treeInsert(this.root, studentEntry, newEntry);
 
        return this;
    }
    
    void treeInsert(BTreeNode node, BTreeEntry entry, BTreeEntry newEntry) {
    	// Non-Leaf Case
    	if (!node.leaf) {
    		// Find which child node to recursively call for the insert
    		int childTarget = -1;
    		for (int i = 0; i < node.keys.length; i++) {
    			if (entry.key < node.keys[i]) {
    				childTarget = i;
    				break;
    			}
    			if (node.keys[i] == 0) {
    				childTarget = i;
    				break;
    			}
    		}
    		// If there is no key > than the new value add to the last (largest) child
    		if (childTarget == -1) {
    			childTarget = node.children.length - 1;
    		}
    		
    		// Insert recursively into correct child node
//    		BTreeEntry test = new BTreeEntry();
//    		newEntry = test;
    		treeInsert(node.children[childTarget], entry, newEntry);
    		
    		// If no child split - newEntry is null and return 
    		if (newEntry == null) return;
    		if (newEntry.key == 0) return;
    		
    		// Else - handle child split
    		
    		// If N has space - add pointer from newChild to this node
    		if (node.n < node.keys.length) {
    			// Insert key in order
    			int keyInsPos = findInsertPosition(node, newEntry);
    			long[] newKeys = new long[node.keys.length];
    			
    			for (int j = 0; j < newKeys.length; j++) {
    	    		if (j < keyInsPos) {
    	    			newKeys[j] = node.keys[j];
    	    		}
    	    		if (j == keyInsPos) {
    	    			newKeys[j] = newEntry.key;
    	    		}
    	    		if (j > keyInsPos) {
    	    			newKeys[j] = node.keys[j-1];
    	    		}
    	    	}
    			node.n += 1;
    			
    			// Insert children pointer in order
    			BTreeNode[] tempChildren = new BTreeNode[node.children.length];
    			int newChildIdx = childTarget + 1;
    			
    			for (int j = 0; j < tempChildren.length; j++) {
    	    		if (j < newChildIdx) {
    	    			tempChildren[j] = node.children[j];
    	    		}
    	    		else if (j == newChildIdx) {
    	    			tempChildren[j] = newEntry.node;
    	    		}
    	    		else if (j > newChildIdx) {
    	    			tempChildren[j] = node.children[j-1];
    	    		}
    	    	}
    			
    			node.keys = newKeys;
    			node.children = tempChildren;
    			
    			newEntry = null;
    			return;
    			
    		} else {
    			// No space in node for new split child node
    			// Split N (2d + 1 KV and 2d + 2 children pointers)
    			BTreeNode nodeTwo = new BTreeNode(node.t, false);
    			
				// Stay N - first d key-values pairs and d+1 children stay
				// Split N2 - last d key-values pairs and d+1 children split
    			splitInsertNode(node, nodeTwo, newEntry, childTarget);
    			
    			// Set newEntry object (smallest KV in N2, pointer to N2)
//    			newEntry = new BTreeEntry(nodeTwo.keys[0], nodeTwo.values[0], nodeTwo);
    			newEntry.key = nodeTwo.keys[0];
    			newEntry.value = nodeTwo.values[0];
    			newEntry.node = nodeTwo;
				// IF N was root...
    			if (root == node) {
    				// Create new node3 w pointer to N and N2 (via newEntry)
    				BTreeNode nodeThree = new BTreeNode(this.t, false);
    				
    				// Push up smallest in N2
    				nodeThree.keys[0] = newEntry.key;
    				nodeThree.values[0] = newEntry.value;
    				
    				// Point the tree.root = new Node3
    				root = nodeThree;
    				
    				// Change node.leaf = false
    				// Set the new node3 children? node, nodeTwo
    				nodeThree.children[0] = node;
    				nodeThree.children[1] = nodeTwo;
    				nodeThree.n = 1;
    				
    				// Change right node to remove key moved up
    				nodeTwo.keys[0] = nodeTwo.keys[1];
    				nodeTwo.keys[1] = 0;
    				nodeTwo.n = 1;
    			}
    			return;
    		}    		
    	}
    	
    	// Node is a leaf case
    	
    	// Check if there is space for key-value
    	if (node.n < node.keys.length) {
    		// Add entry (key-val) to leaf node in key order
    		arrayLeafInsert(node, entry);
    		node.n += 1;
    		newEntry = null;
    		return;
    	}  
    	else {
    		Helpers.p("No space in leaf");
    		
    		// No space in leaf for key-value - split leaf
    		BTreeNode leafTwo = new BTreeNode(node.t, true);
    		
    		// First D entries in old leaf, rest move to new leaf
    		splitInsertLeaf(node, leafTwo, entry);
    		newEntry.key = leafTwo.keys[0];
			newEntry.value = leafTwo.values[0];
			newEntry.node = leafTwo;
    		node.next = leafTwo;
    		
    		// Check if this leaf is also the root...
    		if (this.root == node) {
    			BTreeNode newRoot = new BTreeNode(this.t, false);
    			newRoot.keys[0] = leafTwo.keys[0];
    			newRoot.n += 1;
    			newRoot.children[0] = node;
    			newRoot.children[1] = leafTwo;
    			
    			this.root = newRoot;
    		}
    		return;
    	}
    }
    
    void splitInsertNode(BTreeNode nodeOne, BTreeNode nodeTwo, BTreeEntry newEntry, int childIdx) {
    	int degree = nodeOne.t;
    	
    	// Organize keys
    	int keyInsPost = findInsertPosition(nodeOne, newEntry);
    	long[] newKeys = new long[nodeOne.keys.length + 1];
    	for (int i = 0; i < newKeys.length; i++) {
    		if (i < keyInsPost) {
    			newKeys[i] = nodeOne.keys[i];
    		}
    		else if (i == keyInsPost) {
    			newKeys[i] = newEntry.key;
    		}
    		else if (i > keyInsPost) {
    			newKeys[i] = nodeOne.keys[i-1];
    		}
    	}
    	
    	// Organize children pointers
    	BTreeNode[] tempChildren = new BTreeNode[nodeOne.children.length + 1];
		int newChildIdx = childIdx + 1;
		
		for (int j = 0; j < tempChildren.length; j++) {
    		if (j < newChildIdx) {
    			tempChildren[j] = nodeOne.children[j];
    		}
    		else if (j == newChildIdx) {
    			tempChildren[j] = newEntry.node;
    		}
    		else if (j > newChildIdx) {
    			tempChildren[j] = nodeOne.children[j-1];
    		}
    	}
		
    	// Save subsets of each key and children arrays back to each node
    	nodeOne.keys = new long[2 * degree - 1 ];
    	nodeOne.children = new BTreeNode[2 * degree];
    	nodeOne.n = 0;
    	
    	// Save back keys to each node
    	for (int i = 0; i < degree; i ++) {
    		nodeOne.keys[i] = newKeys[i];
    		nodeOne.n += 1;
    	}
    	for (int i = degree; i < newKeys.length; i++) {
    		nodeTwo.keys[i - degree] = newKeys[i];
    		nodeTwo.n += 1;
    	}
    	
    	// Save back children pointers to each node
    	for (int i = 0; i < tempChildren.length; i++) {
    		if (i < degree + 1) {
    			nodeOne.children[i] = tempChildren[i];
    		} else {
    			nodeTwo.children[i - (degree + 1)] = tempChildren[i];
    		}
    	}
    }
    
    void splitInsertLeaf(BTreeNode leafOne, BTreeNode leafTwo, BTreeEntry entry) {
    	int degree = leafOne.t;
    	int insertPos = findInsertPosition(leafOne, entry);
    	
    	// Make two new arrays for key and value
    	long[] newKeys = new long[leafOne.keys.length + 1];
    	long[] newValues = new long[leafOne.values.length + 1];
    	
    	for (int j = 0; j < newKeys.length; j++) {
    		if (j < insertPos) {
    			newKeys[j] = leafOne.keys[j];
    			newValues[j] = leafOne.values[j];
    		}
    		else if (j == insertPos) {
    			newKeys[j] = entry.key;
    			newValues[j] = entry.value;
    		}
    		else if (j > insertPos) {
    			newKeys[j] = leafOne.keys[j-1];
    			newValues[j] = leafOne.values[j-1];
    		}
    	}
    	
    	// Save subsets of each key/value array back to each leaf
    	leafOne.keys = new long[2 * degree - 1 ];
    	leafOne.values = new long[2 * degree - 1 ];
    	leafOne.n = 0;

//    	//Don't need
//    	leafTwo.keys = new long[2 * degree - 1 ];
//    	leafTwo.values = new long[2 * degree - 1 ];
//    	leafTwo.n = 0;
    	
    	for (int i = 0; i < degree; i ++) {
    		leafOne.keys[i] = newKeys[i];
    		leafOne.values[i] = newValues[i];
    		leafOne.n += 1;
    	}
    	for (int i = degree; i < newKeys.length; i++) {
    		leafTwo.keys[i - degree] = newKeys[i];
    		leafTwo.values[i - degree] = newValues[i];
    		leafTwo.n += 1;
    	}
    }
    
    int findInsertPosition(BTreeNode node, BTreeEntry entry) {
    	int insertPos = -1;
    	// Find insert position
    	for (int i = 0; i < node.n; i++) {
    		if (entry.key < node.keys[i]) {
    			insertPos = i;
    			break;
    		}
    	}
    	if (insertPos == -1) {
    		insertPos = node.n;
    	}
    	
    	return insertPos;
    }
    
    
    void arrayLeafInsert(BTreeNode node, BTreeEntry entry) {
    	int insertPos = findInsertPosition(node, entry);
    	
    	// Create new arrays and insert old + new value
    	long[] newKeys = new long[node.keys.length];
    	long[] newValues = new long[node.values.length];
    	
    	for (int j = 0; j < newKeys.length; j++) {
    		if (j < insertPos) {
    			newKeys[j] = node.keys[j];
    			newValues[j] = node.values[j];
    		}
    		if (j == insertPos) {
    			newKeys[j] = entry.key;
    			newValues[j] = entry.value;
    		}
    		if (j > insertPos) {
    			newKeys[j] = node.keys[j-1];
    			newValues[j] = node.values[j-1];
    		}
    	}
    	
    	// Replace the old arrays on the node with new sorted
    	node.keys = newKeys;
    	node.values = newValues;
    }
    
    
    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
        return true;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        Helpers.p("Printing keys in order: ");
        recursivePrint(root,listOfRecordID);
        return listOfRecordID;
    }
    
    void recursivePrint(BTreeNode node, List<Long> listOfRecordID) {
    	if (node == null) return;
    	
    	for (int i = 0; i < node.children.length; i++) {
    		recursivePrint(node.children[i], listOfRecordID);
    		
    		if (i < node.n && node.leaf) {
    			Helpers.p(node.keys[i] + " ");
    			listOfRecordID.add(node.values[i]);
    		}
    	}
    }
    
    void treeDebugPrint() {
    	
    	ArrayList<ArrayList<BTreeNode>>	outTree = new ArrayList<ArrayList<BTreeNode>>();
    	
    	int level = 0;
    	
    	recursiveDebugPrint(outTree, root, level);
    	
    	for (int i = 0; i < outTree.size(); i++) {
    		ArrayList<BTreeNode> nodeList = outTree.get(i);
    		for (BTreeNode bTreeNode : nodeList) {
				bTreeNode.nodeDebugPrint();
			}
    		System.out.println();
    	}
    	
    }
    
    void recursiveDebugPrint(ArrayList<ArrayList<BTreeNode>> outTree, BTreeNode node, int level) {
    	if (node == null)  return;
    	
    	if (outTree.size() <= level ) {
    		ArrayList<BTreeNode> newList = new ArrayList<BTreeNode>();
    		newList.add(node);
    		outTree.add(level, newList);
    	} else {
    		outTree.get(level).add(node);
    	}
    	
    	if (node.leaf) return;
    	for (int i = 0; i < node.children.length; i++) {
    		recursiveDebugPrint(outTree, node.children[i], level + 1);
    	}
    	
    	
    }
    
}
