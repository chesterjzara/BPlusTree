import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    // field to track the csv DB of the tree
    public String dbPath = "src\\Student.csv";

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

    public BTreeNode treeSearch(BTreeNode parent, long searchKey) {
        // Base - parent is leaf
        if(parent.leaf) {
            return parent;
        }

        for (int i = 0; i < parent.n; i++) {
            long nodeKey = parent.keys[i];
            if (searchKey < nodeKey) {
                return treeSearch(parent.children[i], searchKey);
            }

            if (i == (parent.keys.length - 1)) {
                return treeSearch(parent.children[i+1], searchKey);
            }
        }

        return null;
    }

    BTree insert(Student student) {
    	return insert(student, false);
    }
    
    BTree insert(Student student, boolean addCSV) {
        /**
         * TODO:
         * Implement this function to insert in the B+Tree.
         * Also, insert in student.csv after inserting in B+Tree.
         */
    	Helpers.p("++ Inserting " + student.studentId + " ++");
    	
    	// If there is no root node, create one
    	if (this.root == null) {
    		Helpers.p("Start - No root yet... creating new root");
    		this.root = new BTreeNode(this.t, true);
    	}
    	
    	// Get the student id and record id and insert recursively into BTree
    	long studentId = student.studentId;
    	long recordId = student.recordId;
    	BTreeEntry studentEntry = new BTreeEntry(student.studentId, student.recordId);
    	BTreeEntry newEntry = new BTreeEntry();
    	
    	this.treeInsert(this.root, studentEntry, newEntry);
    	if (Helpers.debug) { this.treeDebugPrint(); }
    	
    	// Add the student ID to the Students.csv file and remove blank lines
    	if (addCSV) {
    		String newStudentLine = newStudentCsvLine(student);
    		addStudentLineToCsv(newStudentLine);
    		deleteCsvLine(-1);
    	}
    	
        return this;
    }
    
    private void addStudentLineToCsv(String newLine) {
    	File csvFile = new File(dbPath);
    	try {
    		FileWriter fw = new FileWriter(csvFile, true);
    		fw.write("\n");
    		fw.write(newLine);
    		fw.close();	
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    private String newStudentCsvLine(Student s) {
    	StringBuilder str = new StringBuilder();
    	str.append(s.studentId);
    	str.append(",");
    	str.append(s.studentName);
    	str.append(",");
    	str.append(s.major);
    	str.append(",");
    	str.append(s.level);
    	str.append(",");
    	str.append(s.age);
    	str.append(",");
    	str.append(s.recordId);
    	
    	return str.toString();
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
    		}
    		// If there is no key > than the new value add to the last (largest) child
    		if (childTarget == -1) {
    			childTarget = node.n;
    		}
    		
    		// Insert recursively into correct child node
    		treeInsert(node.children[childTarget], entry, newEntry);
    		
    		// If no child split - newEntry is null and return 
    		if (newEntry == null) return;
    		if (newEntry.key == 0) return;
    		
    		// Else - handle child split
    		
    		// If N has space - add pointer from newChild to this node
    		if (node.n < node.keys.length) {
    			Helpers.p("Node - Space to add new child entry key to parent node - done.");
    			// Insert key + children in order 
    			insertNodeWithSpace(node, newEntry, childTarget);
    			
    			// Set newEntry to null to avoid recursion - done
    			newEntry.node = null;
    			newEntry.key = 0;
    			return;
    			
    		} else {
    			// No space in node for new split child node
    			Helpers.p("Node - No space in node for split child node - split"
    					+ " node, push mid key to parent, and recurse.");
    			// Split N (2d + 1 KV and 2d + 2 children pointers)
    			BTreeNode nodeTwo = new BTreeNode(node.t, false);
    			
				// Stay N - first d key-values pairs and d+1 children stay
				// Split N2 - last d key-values pairs and d+1 children split
    			splitInsertNode(node, nodeTwo, newEntry, childTarget);
    			
    			// Set newEntry object (smallest KV in N2, pointer to N2)
    			newEntry.key = nodeTwo.keys[0];
    			newEntry.value = nodeTwo.values[0];
    			newEntry.node = nodeTwo;
				
    			// If N was root...
    			if (root == node) {
    				Helpers.p("Node - Split internal node is root - make new root - done.");
    				// Create new root node w/ pointers to node and the new split 
    					// node (via newEntry)
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
    				
    				// Change right node to remove key that moved up - shift all right 1
    				nodeTwo.n -= 1;
    				for (int i = 0; i < nodeTwo.keys.length - 1; i++) {
    					nodeTwo.keys[i] = nodeTwo.keys[i + 1];
    				}
    				nodeTwo.keys[nodeTwo.keys.length - 1] = 0;
    			}
    			return;
    		}    		
    	}
    	
    	// Node is a leaf case
    	
    	// Check if there is space for key-value
    	if (node.n < node.keys.length) {
    		Helpers.p("Leaf - Space to add key to leaf - done.");
    		// Add entry (key-val) to leaf node in key order
    		arrayInsertLeaf(node, entry);
    		
    		// Set newEntry to null - no new children to account for in recursion - done
    		newEntry.node = null;
    		newEntry.key = 0;
    		return;
    	}  
    	else {
    		Helpers.p("Leaf - No space in leaf - split leaf");
    		
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
    			Helpers.p("Leaf - Split leaf is the root - make a new root to hold" +
    					" split leaf children - done.");
    			// Set 2 split leaves as children of new root
    			BTreeNode newRoot = new BTreeNode(this.t, false);
    			// Copy first key in right node up as the parent root key
    			newRoot.keys[0] = leafTwo.keys[0];
    			newRoot.n += 1;
    			newRoot.children[0] = node;
    			newRoot.children[1] = leafTwo;
    			
    			this.root = newRoot;
    			// If the root is also a leaf - there is no recursion and we return
    		}
    		return;
    	}
    }
    
    void insertNodeWithSpace(BTreeNode node, BTreeEntry newEntry, int childTarget) {
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
    	nodeOne.keys = new long[2 * degree];
    	nodeOne.children = new BTreeNode[2 * degree + 1];
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
    	leafOne.keys = new long[leafOne.keys.length];
    	leafOne.values = new long[leafOne.values.length];
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
    
    // Create new arrays and insert old + new keys + values
    void arrayInsertLeaf(BTreeNode node, BTreeEntry entry) {
    	int insIdx = findInsertPosition(node, entry);
    	
    	// Create new arrays and insert old + new value
    	long[] newKeys = new long[node.keys.length];
    	long[] newValues = new long[node.values.length];
    	
    	// Copy keys and insert new key in proper order
    	System.arraycopy(node.keys, 0, newKeys, 0, insIdx);
    	newKeys[insIdx] = entry.key;
    	System.arraycopy(node.keys, insIdx, newKeys, insIdx + 1, 
    			node.keys.length - insIdx - 1);
    	
    	// Copy values and insert new values in proper order
    	System.arraycopy(node.values, 0, newValues, 0, insIdx);
    	newValues[insIdx] = entry.value;
    	System.arraycopy(node.values, insIdx, newValues, insIdx + 1, 
    			node.values.length - insIdx - 1);
    	
    	// Replace old arrays on the node with the new sorted including the new key/value
    	node.keys = newKeys;
    	node.values = newValues;
    	node.n += 1;
    }
    
    boolean delete(long studentId) {
        /**
         * TODO:
         * Implement this function to delete in the B+Tree.
         * Also, delete in student.csv after deleting in B+Tree, if it exists.
         * Return true if the student is deleted successfully otherwise, return false.
         */
    	Helpers.p("-- Deleting " + studentId + " --");
    	
    	BTreeEntry studentEntry = new BTreeEntry();
    	studentEntry.key = studentId;
    	BTreeEntry oldChildEntry = new BTreeEntry();
    	
    	treeDelete(null, root, studentEntry, oldChildEntry);
    	
    	if (Helpers.debug) { this.treeDebugPrint(); }
    	
    	// TODO - Do csv edits here
    	deleteCsvLine(studentId);
    	
    	return true;
    }
    
    void deleteCsvLine(long delStudId) {
    	File srcFile = new File(dbPath);
    	String tempPath = "src\\temp.txt";
    	File newFile = new File(tempPath);
    	
    	String line;
    	
    	try {
			// Setup IO for new files
    		FileWriter newFw = new FileWriter(tempPath, true);
			BufferedWriter newBw = new BufferedWriter(newFw);
			PrintWriter newPw = new PrintWriter(newBw);
			
			// Setup IO to read in old, source csv file
			FileReader srcFr = new FileReader(dbPath);
			BufferedReader srcBr = new BufferedReader(srcFr);
			
			// Increment line in srcFile
			while ((line = srcBr.readLine()) != null) {
				//
				long studentID = -2;
				String[] lineVals = line.split(",", 6);
				try {
					studentID = Long.parseLong(lineVals[0].trim());
				}catch (NumberFormatException e) {
					Helpers.p("Remove blank line from csv file.");				
				}
				
				
				if (studentID != delStudId && studentID != -2) {
					newPw.println(line);
				}
			}
			newPw.flush();
			newPw.close();
			newFw.close();
			srcBr.close();
			newBw.close();
			newFw.close();
			
			srcFile.delete();
			File swap = new File(dbPath);
			newFile.renameTo(swap);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
    }
    
    void treeDelete(BTreeNode parent, BTreeNode node, BTreeEntry entry,
    		BTreeEntry oldChildEntry) {
    	int minKeys = node.keys.length / 2;
    	// If node is non-leaf node
    	if (!node.leaf) {
    		// Choose correct subtree based on key
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
    		// Recursive delete on sub-tree node
    		treeDelete(node, node.children[childTarget], entry, oldChildEntry);
    		
    		// If - No child deletion - return - we are done!
    		if (oldChildEntry == null || oldChildEntry.node == null) {
    			return;
    			
    		} else {
    			// Else - a child node was discarded
    			
    			// Check for an empty former root
    			if (node.numKeys() < 1) {
    				Helpers.p("Node - Hit empty root - done.");
    				return;
    			}
    			
    			// Remove oldChildEntry from this node's children + key arrays
    			BTreeNode removedNode = oldChildEntry.node;
    			int parentKeyIdx = findParentKeyForNode(node, removedNode);
    			removeParentKeyAndChild(node, parentKeyIdx);
    			
    			// If node has entries to spare
    			if (node.n > minKeys) {
    				Helpers.p("Node - Delete from parent w/ spare keys - done.");
    				// Set oldChildEntry to null and return
    				oldChildEntry = null;
    				return;
    				
    			} else {
    				// Else - merge node to sibling
    				// Get sibling S of node N (via parentNode) - left and right
    				BTreeNode rightSib = findRightSibling(parent, node);
        			BTreeNode leftSib = findLeftSibling(parent, node);
        			
        			// If S has extra entries
        			if (rightSib != null && rightSib.n - 1 >= minKeys) { 
        				Helpers.p("Node - Delete from parent w/ right sib redistribute - done.");
        				// Redistribute evenly to N and S - through parent
        				redistNodes(parent, node, rightSib);
        				
        				// Replace key in parent node w/ low-key in new right node
        				int parentChildIdx = findParentKeyForNode(parent, rightSib);
        				long tempKey = parent.keys[parentChildIdx - 1];
        				parent.keys[parentChildIdx - 1] = node.keys[node.n - 1];
        				node.keys[node.n - 1] = tempKey;
        				
        				// Set oldChildEntry to null and return
        				oldChildEntry.node = null;
        				return;
        				
        			} else if (leftSib != null && leftSib.n - 1 >= minKeys) {
        				Helpers.p("Node - Delete from parent w/ left sib redistribute - done.");
        				// Redistribute evenly to N and S - through parent
        				redistNodes(parent, leftSib, node);
        				
        				// Replace key in parent node w/ low-key in new right node
        				int parentChildIdx = findParentKeyForNode(parent, node);
        				long tempKey = parent.keys[parentChildIdx - 1];
        				parent.keys[parentChildIdx - 1] = node.keys[0];
        				node.keys[0] = tempKey;
        				
        				// Set oldChildEntry to null and return
        				oldChildEntry.node = null;
        				return;
        				
        			} else {
        				// Else - no extra entries = merge N and S
        				// oldChildEntry = current entry in parent for M (RHS node)
        				// "pull" splitting key from parent into left node
        				// move all entries form M/N to node on left
        				// discard empty node M/N, return        				

        				// Merge with Right sibling
        				if (rightSib != null) {
        					Helpers.p("Node - Delete from parent w/ right sib merge - recurse");
        					oldChildEntry.node = rightSib;
        					pullParentSplitKey(parent, node, rightSib);
        					mergeKeysAndChildrenToLeftNode(node, rightSib);
        					//discardEmptyNode(parent, node, rightSib);
        					
        					BTreeNode newRightSib = findRightSibling(parent, node);
        					node.next = newRightSib;
            				return;
        				} 
        				// Merge with Left sibling
        				else if (leftSib != null) {
        					Helpers.p("Node - Delete from parent w/ left sib merge - recurse");
        					oldChildEntry.node = node;
        					pullParentSplitKey(parent, leftSib, node);
        					mergeKeysAndChildrenToLeftNode(leftSib, node);
        					
        					//discardEmptyNode(parent, leftSib, node);
        						// Or is this handled in stack by recursion?
        					
        					BTreeNode newRightSib = findRightSibling(parent, leftSib);
        					leftSib.next = newRightSib;
            				return;
        					
        				} else {
        					Helpers.p("***ERROR - No sibling found for inner node merge***");
        				}
        			}
    			}			
    		}
    	}		
    	
    	// If node is a leaf node
    	if (node.leaf) {
    		// If - L has entries to spare - (keys.length / 2 = too few)
    		if (node.n - 1 >= minKeys) {
    			Helpers.p("Leaf - Deleted from leaf with spare keys - done.");
    			// Remove entry, set oldChildEntry to null and return
    			arrayLeafDelete(node, entry);
    			node.n -= 1;
    			oldChildEntry = null;
    		} else {
    		// Else - No entries to spare
    			// Get all sibling S of L (via parentNode)
    			BTreeNode rightSib = findRightSibling(parent, node);
    			BTreeNode leftSib = findLeftSibling(parent, node);
    			
    			// If - S has extra entries
	    			// Remove key to delete
    				// Redistribute evenly between S and L
	    			// Find entry in parent for node on right (S or L - now called M)
	    			// Replace key value in parent entry for new low-key in M
	    			
    			if (rightSib != null && rightSib.n - 1 >= minKeys) {
    				Helpers.p("Leaf - No spare keys - redistribute from right sib - done.");
    				// Remove the key to delete
    				arrayLeafDelete(node, entry);
        			node.n -= 1;
        			
        			// Redistribute the keys-values between node and right sibling
    				redistLeaves(node, rightSib, true);
    				
    				// Replace key in parent node w/ low-key in right node
    				int parentChildIdx = findParentKeyForNode(parent, rightSib);
    				parent.keys[parentChildIdx - 1] = rightSib.keys[0];
    				
    				// Set oldChildEntry to null and return
    				oldChildEntry = null;
    				return;
    				
    			} else if (leftSib != null && leftSib.n - 1 >= minKeys) {
    				Helpers.p("Leaf - No spare keys redistribute from left sib - done.");
    				// Remove the key to delete
    				arrayLeafDelete(node, entry);
        			node.n -= 1;
        			
        			// Redistribute the keys-values between node and right sibling
    				redistLeaves(node, leftSib, false);
    				
    				// Replace key in parent node w/ low-key in right node
    				int parentKeyIdx = findParentKeyForNode(parent, node);
    				parent.keys[parentKeyIdx - 1] = node.keys[0];
    				
    				// Set oldChildEntry to null and return
    				oldChildEntry = null;
    				return;
    				
    			} else {
    				arrayLeafDelete(node, entry);
    				node.n -= 1;
    				// Else - merge L and S (try right, then left)
    				BTreeNode mergeNode;
    				BTreeNode targetNode;
    				if (rightSib != null) {
    					Helpers.p("Leaf - No spare keys merge from right sib - recurse");
    					mergeNode = rightSib;
    					targetNode = node;
    				} else  {
    					Helpers.p("Leaf - No spare keys merge from left sib - recurse");
    					mergeNode = node;
    					targetNode = leftSib;
    				}
    				// oldChildEntry = current entry in Parent for M
    				oldChildEntry.node = mergeNode;
    				// Do we need key/value here in oldChildEntry??
    				
    				// move all entries from M to node on left
    				mergeKeysToLeftLeaf(targetNode, mergeNode);
    				
    				// Discard empty M node - handled by upstack by recursive node checks 
    				
    				// Adjust sibling pointers, return	
    				BTreeNode newRightSib = findRightSibling(parent, targetNode);
    				targetNode.next = newRightSib;
    				return;
    			}
    		}
    	}
    }
    
    void pullParentSplitKey(BTreeNode parent, BTreeNode left, BTreeNode right) {
    	int parentSplitChildIdx = findParentKeyForNode(parent, right);
    	int parentSplitKeyIdx = parentSplitChildIdx - 1;
    	
    	BTreeEntry insertFromParent = new BTreeEntry(parent.keys[parentSplitKeyIdx]);
    	int insIdx = findInsertPosition(left, insertFromParent);
    	
    	// Create new arrays and insert old + new value
    	long[] newKeys = new long[left.keys.length];
    	// Copy keys without deleted key
    	System.arraycopy(left.keys, 0, newKeys, 0, insIdx);
    	newKeys[insIdx] = insertFromParent.key;
    	System.arraycopy(left.keys, insIdx, newKeys, insIdx + 1, left.keys.length - insIdx - 1);
    	
    	// Replace the old arrays on the node with new sorted
    	left.keys = newKeys;
    	left.n += 1;
    	
    	// Remove the key from the parent
    	long[] newParentKeys = new long[parent.keys.length];
    	System.arraycopy(parent.keys, 0, newParentKeys, 0, parentSplitKeyIdx);
    	System.arraycopy(parent.keys, parentSplitKeyIdx + 1, newParentKeys, 
    			parentSplitKeyIdx, parent.keys.length - parentSplitKeyIdx - 1);
    	
    	parent.keys = newParentKeys;
    	parent.n -= 1;
    	
    	// If parent is empty and it was root, replace
    	if (parent.n < 1 && parent == this.root) {
    		this.root = left;
    	}
    }
    
    void redistNodes(BTreeNode parent, BTreeNode left, BTreeNode right) {
    	int totalKeys = left.n + right.n + 1;
    	int totalChildren = totalKeys + 2;
    	long[] tempKeys = new long[totalKeys];
    	BTreeNode[] tempChildren = new BTreeNode[totalChildren];
    	
    	// Copy keys/children into temp arrays
    	System.arraycopy(left.keys, 0, tempKeys, 0, left.n);
		System.arraycopy(right.keys, 0, tempKeys, left.n, right.n);
		System.arraycopy(left.children, 0, tempChildren, 0, left.n + 1);
		System.arraycopy(right.children, 0, tempChildren, left.n + 1, right.n + 1);
		
		
		long[] newLeftKeys = new long[left.keys.length];
		long[] newRightKeys = new long[right.keys.length];
		left.n = 0;
		right.n = 0;
		int rightCnt = 0;
		int rightChildCnt = 0;
		
		BTreeNode[] newLeftChildren = new BTreeNode[left.children.length];
		BTreeNode[] newRightChildren = new BTreeNode[right.children.length];
		
		// Redistribute the keys into each node - left and right
		for (int i = 0; i < totalKeys; i++) {
			if (tempKeys[i] == 0 ) {
				continue;
			}
			if (i < totalKeys / 2) {
				newLeftKeys[i] = tempKeys[i];
				left.n += 1;
			} else {
				newRightKeys[rightCnt] = tempKeys[i];
				right.n += 1;
				rightCnt += 1;
			}
		}
		
		// Redistribute the children into each node - left and right
		for (int i = 0; i < totalChildren; i++) {
			if (tempChildren[i] == null) {
				continue;
			}
			if (i < totalChildren / 2) {
				newLeftChildren[i] = tempChildren[i];
			} else {
				newRightChildren[rightChildCnt] = tempChildren[i];
				rightChildCnt += 1;
			}
		}
		
		// Save the new keys + children arrays back to the left and right nodes
		left.keys = newLeftKeys;
		right.keys = newRightKeys;
		left.children = newLeftChildren;
		right.children = newRightChildren;
    }
    
    void removeParentKeyAndChild(BTreeNode parent, int removeIdx) {
    	long[] tempKeys = new long[parent.keys.length];
    	BTreeNode[] tempChildren = new BTreeNode[parent.children.length];
    	
    	int keyRemove = removeIdx - 1;
    	System.arraycopy(parent.keys, 0, tempKeys, 0, keyRemove);
		System.arraycopy(parent.keys, keyRemove + 1, tempKeys, keyRemove, 
				parent.keys.length - keyRemove - 1);

		System.arraycopy(parent.children, 0, tempChildren, 0, removeIdx);
		System.arraycopy(parent.children, removeIdx + 1, tempChildren, removeIdx, 
				parent.children.length - removeIdx - 1);
		
		parent.keys = tempKeys;
		parent.children = tempChildren;
		parent.n -= 1;
    }
    
    void mergeKeysToLeftLeaf(BTreeNode targetNode, BTreeNode mergeNode) {
    	// Create new temp arrays for combined keys and values 
    	long[] tempKeys = new long[targetNode.keys.length];
    	long[] tempValues = new long[targetNode.values.length];
    	
    	// Copy all keys and values into combined temp arrays
		System.arraycopy(targetNode.keys, 0, tempKeys, 0, targetNode.n);
		System.arraycopy(mergeNode.keys, 0, tempKeys, targetNode.n, mergeNode.n);
		System.arraycopy(targetNode.values, 0, tempValues, 0, targetNode.n);
		System.arraycopy(mergeNode.values, 0, tempValues, targetNode.n, mergeNode.n);
    	
		targetNode.n = targetNode.n + mergeNode.n;
		targetNode.keys = tempKeys;
		targetNode.values = tempValues;
    }
    
    void mergeKeysAndChildrenToLeftNode(BTreeNode targetNode, BTreeNode mergeNode) {
    	// Create new temp arrays for combined keys and values 
    	long[] tempKeys = new long[targetNode.keys.length];
    	BTreeNode[] tempChildren = new BTreeNode[targetNode.children.length];
    	
    	// Copy all keys into combined temp array
		System.arraycopy(targetNode.keys, 0, tempKeys, 0, targetNode.n);
		System.arraycopy(mergeNode.keys, 0, tempKeys, targetNode.n, mergeNode.n);
		// Copy all children into combined temp array
		int tarChildCnt = targetNode.numChildren();
		int mrgChildCnt = mergeNode.numChildren();
		System.arraycopy(targetNode.children, 0, tempChildren, 0, tarChildCnt);
		System.arraycopy(mergeNode.children, 0, tempChildren, tarChildCnt, mrgChildCnt);
		
		targetNode.n = targetNode.n + mergeNode.n;
		targetNode.keys = tempKeys;  	
		targetNode.children = tempChildren;
    }
    
    
    int findParentKeyForNode(BTreeNode parent, BTreeNode node) {
    	int idx = -1;
    	if (parent == null || node == null) {
    		Helpers.p("ERROR - Missing input parent and/or child node");
    	}
    	for (int i = 0; i < parent.keys.length + 1; i++) {
    		if (parent.children[i] == node) {
    			return i;
    		}
    	}
    	return idx;
    }
    
    
    void redistLeaves(BTreeNode node, BTreeNode sibNode, boolean right) {
    	// Create new temp arrays for combined keys and values before redist
    	int totalKeys = node.n + sibNode.n;
    	long[] tempKeys = new long[totalKeys];
    	long[] tempValues = new long[totalKeys];
    	
    	// Determine which node is left and right
    	BTreeNode leftNode;
		BTreeNode rightNode;
		if (right) {
    		leftNode = node;
    		rightNode = sibNode;
    	} else {
    		leftNode = sibNode;
    		rightNode = node;
    	}
    	
		// Copy all keys and values into combined temp arrays
		System.arraycopy(leftNode.keys, 0, tempKeys, 0, leftNode.n);
		System.arraycopy(rightNode.keys, 0, tempKeys, leftNode.n, rightNode.n);
		System.arraycopy(leftNode.values, 0, tempValues, 0, leftNode.n);
		System.arraycopy(rightNode.values, 0, tempValues, leftNode.n, rightNode.n);
		
		long[] newLeftKeys = new long[leftNode.keys.length];
		long[] newLeftValues = new long[leftNode.values.length];
		long[] newRightKeys = new long[rightNode.keys.length];
		long[] newRightValues = new long[rightNode.values.length];
		int sibCnt = 0;
		leftNode.n = 0;
		rightNode.n = 0;
		
		// Redistribute the keys and values into each node - left and right
		for (int i = 0; i < totalKeys; i++) {
			if (i < totalKeys / 2) {
				newLeftKeys[i] = tempKeys[i];
				newLeftValues[i] = tempValues[i];
				leftNode.n += 1;
			} else {
				newRightKeys[sibCnt] = tempKeys[i];
				newRightValues[sibCnt] = tempValues[i];
				rightNode.n += 1;
				sibCnt += 1;
			}
		}
		
		// Save the new keys + value arrays back to the left and right nodes
		leftNode.keys = newLeftKeys;
		leftNode.values = newLeftValues;
		rightNode.keys = newRightKeys;
		rightNode.values = newRightValues;
    }

    BTreeNode findRightSibling(BTreeNode parent, BTreeNode node) {
    	if (parent == null) return null;
    	
    	// Find the index of node within parent's children
    	int idx = -1;
    	for (int i = 0; i < parent.keys.length + 1; i++) {
    		if (parent.children[i] == node) {
    			idx = i;
    			break;
    		}
    	}
    	
    	// Find the next largest child on parent 
    	if (idx < parent.keys.length) {
    		return parent.children[idx + 1];
    	}
    	// (unless this is the rightmost)
    	return null;
    }
    
    
    BTreeNode findLeftSibling(BTreeNode parent, BTreeNode node) {
    	if (parent == null) return null;
    	
    	// Find the index of node with parent's children
    	int idx = -1;
    	for (int i = 0; i < parent.keys.length + 1; i++) {
    		if (parent.children[i] == node) {
    			idx = i;
    			break;
    		}
    	}
    	
    	// Find the next largest child on parent 
    	if (idx > 0) {
    		return parent.children[idx - 1];
    	}
    	// (unless this is the rightmost)
    	return null;
    }
    
    
    void arrayLeafDelete(BTreeNode node, BTreeEntry entry) {
    	// Find Delete Index
    	int delIdx = Arrays.binarySearch(node.keys, entry.key);
    	
    	// Create new arrays and remove the old key + value
    	long[] newKeys = new long[node.keys.length];
    	long[] newValues = new long[node.values.length];
    	
    	// Copy keys without deleted key
    	System.arraycopy(node.keys, 0, newKeys, 0, delIdx);
    	System.arraycopy(node.keys, delIdx + 1, newKeys, delIdx, node.keys.length - delIdx - 1);
    	
    	// Copy values without deleted value
    	System.arraycopy(node.values, 0, newValues, 0, delIdx);
    	System.arraycopy(node.values, delIdx + 1, newValues, delIdx, node.values.length - delIdx - 1);
    	
    	// Replace the old arrays on the node with new arrays
    	node.keys = newKeys;
    	node.values = newValues;
    }

    List<Long> print() {

        List<Long> listOfRecordID = new ArrayList<>();

        /**
         * TODO:
         * Implement this function to print the B+Tree.
         * Return a list of recordIDs from left to right of leaf nodes.
         *
         */
        Helpers.p("Printing RecordIDs (values) in order: ");
        recursivePrint(root,listOfRecordID);
        return listOfRecordID;
    }
    
    
    
    void recursivePrint(BTreeNode node, List<Long> listOfRecordID) {
    	if (node == null) return;
    	
    	for (int i = 0; i < node.children.length; i++) {
    		recursivePrint(node.children[i], listOfRecordID);
    		
    		if (i < node.n && node.leaf) {
    			//Helpers.p(node.keys[i] + " ");
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
				bTreeNode.nodeDebugPrint(true);
			}
    		System.out.println();
    	}
    	System.out.println(); // Extra line for spacing
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
