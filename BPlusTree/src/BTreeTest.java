import java.util.List;

public class BTreeTest {

	public static void main(String[] args) {
		Helpers.debug = true;
		Helpers.p("Debug printing enabled!");
		Helpers.p("");
		
//		BTree testTree = new BTree(2);
//		Student stud1 = new Student(2, 12, "CJ", "Math", "SR", 111);
//		testTree.insert(stud1);
//		
//		long foundRid = testTree.search(1);
//		
//		System.out.println("Found rec id:" + foundRid);
		
//		System.out.println("Test array insert");
//		testArrayInsert();
//		System.out.println("Test Root Insert");
//		testInsertRoot();
		
//		testLeafDelete();
		
//		testDelete();
		
		testRemoveParentKeyAndChild();
	}
	
	public static void testInsertRoot() {
		
		BTree tree = new BTree(2);
		for (int i = 1; i < 11; i++) {
			Student stud = new Student(i, 12, "CJ", "Math", "SR", i*11);
			tree.insert(stud);
		}
		tree.treeDebugPrint();
		tree.print();
	}
	
	public static void testArrayInsert() {
		BTree tree = new BTree(3);
		
		BTreeNode test = new BTreeNode(3, true);
		test.keys[0] = 2;
		test.keys[1] = 3;
		test.keys[2] = 4;
		test.keys[3] = 6;
		test.values[0] = 22;
		test.values[1] = 33;
		test.values[2] = 44;
		test.values[3] = 66;
		test.n = 4;
		
		BTreeEntry entry = new BTreeEntry(7, 11);
		
		System.out.println("Before");
		for (int i = 0; i < test.keys.length; i++) {
			System.out.println(test.keys[i] + "-" + test.values[i]);
		}
		
		tree.arrayInsertLeaf(test, entry);
		
		System.out.println("After");
		for (int i = 0; i < test.keys.length; i++) {
			System.out.println(test.keys[i] + "-" + test.values[i]);
		}

//		entry = new BTreeEntry(1, 11);
//		tree.arrayInsertLeaf(test, entry);
//		System.out.println("After After");
//		for (int i = 0; i < test.keys.length; i++) {
//			System.out.println(test.keys[i] + "-" + test.values[i]);
//		}
	}
	
	public static void testLeafDelete() {
		BTree tree = new BTree(3);
		BTreeNode test = new BTreeNode(3, true);
		test.keys[0] = 2;
		test.keys[1] = 3;
		test.keys[2] = 4;
		test.keys[3] = 6;
		test.keys[4] = 8;
		test.values[0] = 22;
		test.values[1] = 33;
		test.values[2] = 44;
		test.values[3] = 66;
		test.n = 3;
		BTreeEntry entry = new BTreeEntry(8, 44);
		
		tree.arrayLeafDelete(test, entry);
		
	}


	public static void testDelete() {
		BTree tree = new BTree(2);
		for (int i = 1; i < 4; i++) {
			Student stud = new Student(i, 12, "CJ", "Math", "SR", i*11);
			tree.insert(stud);
		}
		tree.treeDebugPrint();
		System.out.println("Delete 3");
		tree.delete(3);
		tree.treeDebugPrint();
		tree.insert(new Student(10, 12, "cj", "CS", "SR", 110));
		tree.delete(1);
		tree.treeDebugPrint();
	}
	
	public static void testRemoveParentKeyAndChild() {
		BTree tree = new BTree(2);
		tree.insert(new Student(22, 12, "cj", "CS", "SR", 22));
		tree.insert(new Student(24, 12, "cj", "CS", "SR", 24));
		tree.insert(new Student(27, 12, "cj", "CS", "SR", 27));
		tree.insert(new Student(29, 12, "cj", "CS", "SR", 29));
		tree.insert(new Student(25, 12, "cj", "CS", "SR", 25));
		tree.delete(24);
		tree.insert(new Student(30, 12, "cj", "CS", "SR", 30));
		tree.insert(new Student(26, 12, "cj", "CS", "SR", 26));
		tree.delete(30);
		tree.delete(27);
		tree.insert(new Student(32, 12, "cj", "CS", "SR", 32));
		tree.insert(new Student(34, 12, "cj", "CS", "SR", 34));
		tree.insert(new Student(40, 12, "cj", "CS", "SR", 40));
		tree.insert(new Student(50, 12, "cj", "CS", "SR", 50));
		tree.insert(new Student(60, 12, "cj", "CS", "SR", 60));
		tree.insert(new Student(70, 12, "cj", "CS", "SR", 70));
		tree.insert(new Student(55, 12, "cj", "CS", "SR", 55));
		tree.insert(new Student(54, 12, "cj", "CS", "SR", 54));
		tree.insert(new Student(56, 12, "cj", "CS", "SR", 56));
		tree.insert(new Student(57, 12, "cj", "CS", "SR", 57));
		tree.insert(new Student(65, 12, "cj", "CS", "SR", 65));
		tree.insert(new Student(80, 12, "cj", "CS", "SR", 80));
		tree.delete(22);
		tree.delete(57);
		tree.delete(70);
		tree.insert(new Student(42, 12, "cj", "CS", "SR", 42));
		tree.insert(new Student(43, 12, "cj", "CS", "SR", 43));
		tree.delete(54);
		
		List<Long> listOfRecordID = tree.print();
		for (Long recID : listOfRecordID) {
			System.out.print(recID + " >> ");
		}
		
	}
}
