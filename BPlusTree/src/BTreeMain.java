import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Random;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Random;

/**
 * Main Application.
 */
public class BTreeMain {

    public static void main(String[] args) {
    	// TODO - variable to print debugging info - set to false for final version
    	Helpers.debug = true;
    	HashSet<Long> pastRandom =  new HashSet<Long>();

    	
        /** Read the input file -- input.txt */
        Scanner scan = null;
        try {
            scan = new Scanner(new File("src/input.txt"));
        } catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }

        /** Read the minimum degree of B+Tree first */

        int degree = scan.nextInt();

        BTree bTree = new BTree(degree);

        /** Reading the database student.csv into B+Tree Node*/
        List<Student> studentsDB = getStudents();

        for (Student s : studentsDB) {
            bTree.insert(s);
        }

        /** Start reading the operations now from input file*/
        try {
            while (scan.hasNextLine()) {
                Scanner s2 = new Scanner(scan.nextLine());

                while (s2.hasNext()) {

                    String operation = s2.next();

                    switch (operation) {
                        case "insert": {

                            long studentId = Long.parseLong(s2.next());
                            String studentName = s2.next() + " " + s2.next();
                            String major = s2.next();
                            String level = s2.next();
                            int age = Integer.parseInt(s2.next());
                            /** DONE: Write a logic to generate recordID*/
                            // Need to parse the recordID from the input file to 
                            	//avoid throwing off the count
                            long recordID = Long.parseLong(s2.next());
                            
                            // Get a random recordID and check it is unused
                            Random r = new Random();
                            recordID = r.nextLong(studentsDB.size(),100);
                            while (pastRandom.contains(recordID)) {
                            	recordID = r.nextLong(studentsDB.size(),100);
                            }
                            pastRandom.add(recordID);
                            
                            // Create a Student object and insert into btree + csv
                            Student s = new Student(studentId, age, studentName, major, level, recordID);
                            bTree.insert(s, true);

                            break;
                        }
                        case "delete": {
                            long studentId = Long.parseLong(s2.next());
                            boolean result = bTree.delete(studentId);
                            if (result)
                                System.out.println("Student deleted successfully.");
                            else
                                System.out.println("Student deletion failed.");

                            break;
                        }
                        case "search": {
                            long studentId = Long.parseLong(s2.next());
                            long recordID = bTree.search(studentId);
                            if (recordID != -1)
                                System.out.println("Student exists in the database at " + recordID);
                            else
                                System.out.println("Student does not exist.");
                            break;
                        }
                        case "print": {
                            List<Long> listOfRecordID = new ArrayList<>();
                            listOfRecordID = bTree.print();
                            System.out.println("List of recordIDs in B+Tree " + listOfRecordID.toString());
                        }
                        default:
                            System.out.println("Wrong Operation");
                            break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<Student> getStudents() {

        /** DONE:
         * Extract the students information from "Students.csv"
         * return the list<Students>
         */
    	
    	// Test to check the working directory for relative file read in
    	//System.out.println(new File(".").getAbsolutePath());
    	
        List<Student> studentList = new ArrayList<>();
        
        try (Scanner scanner = new Scanner(new File("src\\Student.csv"))) {
        	while (scanner.hasNextLine()) {
        		// Put comma-delim line into an array 
        		String line = scanner.nextLine();
        		String[] values = line.split(",", 6);
        		
        		try {
        			// Parse each element of line array to student fields
        			long studentID = Long.parseLong(values[0].trim());
        			String studentName = values[1];
        			String major = values[2];
        			String level = values[3];
        			int age = Integer.parseInt(values[4]);
        			long recordID = Long.parseLong(values[5].trim());
        			
        			// Create new Student object and add to the list
        			Student student = new Student(studentID, age, studentName, 
        					major, level, recordID);
        			studentList.add(student);
        		
        		} catch (NumberFormatException e) {
        			System.out.println("Error parsing line: " + line);
        			e.printStackTrace();
        		}
        	}
        	
        	// Test line to print out the studentList
        	Helpers.p("Student List:");
        	for (Student student : studentList) {
        		Helpers.p(student);
        	}
        } catch (FileNotFoundException e) {
        	e.printStackTrace();
        }
        	
        return studentList;
    }
}
