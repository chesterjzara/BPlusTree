
public class BTreeTest {

	public static void main(String[] args) {
		Helpers.debug = true;
		Helpers.p("Test debug");
		
//		BTree testTree = new BTree(2);
//		Student stud1 = new Student(2, 12, "CJ", "Math", "SR", 111);
//		testTree.insert(stud1);
//		
//		long foundRid = testTree.search(1);
//		
//		System.out.println("Found rec id:" + foundRid);
		
//		System.out.println("Test array insert");
//		testArrayInsert();
		System.out.println("Test Root Insert");
		testInsertRoot();
	}
	
	public static void testInsertRoot() {
		
		BTree tree = new BTree(2);
		Student stud1 = new Student(1, 12, "CJ", "Math", "SR", 111);
		Student stud2 = new Student(2, 12, "CJ", "Math", "SR", 222);
		Student stud3 = new Student(3, 12, "CJ", "Math", "SR", 333);
		Student stud4 = new Student(4, 12, "CJ", "Math", "SR", 444);
		tree.insert(stud1);
		tree.insert(stud2);
		tree.insert(stud3);
		tree.insert(stud4);		
	}
	
	public static void testArrayInsert() {
		BTree tree = new BTree(3);
		
		BTreeNode test = new BTreeNode(3, true);
		test.keys[0] = 2;
		test.keys[1] = 3;
		test.keys[2] = 4;
		//test.keys[3] = 6;
		test.values[0] = 22;
		test.values[1] = 33;
		test.values[2] = 44;
		//test.values[3] = 66;
		test.n = 3;
		
		BTreeEntry entry = new BTreeEntry(7, 11);
		
		System.out.println("Before");
		for (int i = 0; i < test.keys.length; i++) {
			System.out.println(test.keys[i] + "-" + test.values[i]);
		}
		
		tree.arrayInsert(test, entry);
		
		System.out.println("After");
		for (int i = 0; i < test.keys.length; i++) {
			System.out.println(test.keys[i] + "-" + test.values[i]);
		}
	}
}

