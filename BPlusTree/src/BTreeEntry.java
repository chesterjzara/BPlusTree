
/**
 * BTreeEntry class created to track either new key-value entries or nodes
 * created/deleted during tree balancing operations.
 * 
 * @author chest
 *
 */
public class BTreeEntry {
	long key;
	long value;
	BTreeNode node;
	
	public BTreeEntry(long key, long value, BTreeNode node) {
		this.key = key;
		this.value = value;
		this.node = node;
	}
	
	public BTreeEntry(long key, BTreeNode node) {
		this.key = key;
		this.node = node;
	}
	
	public BTreeEntry(long key, long value) {
		this.key = key;
		this.value = value;
		this.node = null;
	}
	
	public BTreeEntry(long key) {
		this.key = key;
		this.node = null;
	}
	
	public BTreeEntry() {}
}
