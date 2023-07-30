
/**
 * Helper class created to add a debugging variable and wrap a print function
 * 
 * @author chest
 *
 */
public class Helpers {
	static boolean debug = false;
	
		static void p(Object o) {
			if (debug == true)  {
				System.out.println(o);
			}
		}
}
