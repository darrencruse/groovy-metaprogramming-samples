package com.mop.tracing
import groovy.lang.GroovyInterceptable;

//@Mixin(WebSequenceDiagrammer)
public class WebSequenceDiagrammedClass implements GroovyInterceptable {

    String heading = "";
    int count = 0;
	
	public int add(int a, int b) { a + b } 	
	
    private void printItem(String item) {
        System.out.println("item " + count + ": " + item);
        count++;
    }

    public void printItems(String[] items) {
        System.out.println(heading);
        for(String item: items) {
            printItem(item);
        }
    }   
}

// @Mixin(WebSequenceDiagrammer)
public class WebSequenceDiagramDemo {
	public demo() {
	
		WebSequenceDiagrammedClass.mixin WebSequenceDiagrammer		
		// WebSequenceDiagramDemo.mixin WebSequenceDiagrammer
		
		def args = (String[])["a", "b", "c"]
		WebSequenceDiagrammedClass sc = new WebSequenceDiagrammedClass();
		
		// invoke a simple method returning a value:
		println sc.add(2,4);

		// set a simple property:
		sc.heading = "Test";
		
		// You can invoke methods directly with ".&" and set properties directly with ".@"
		//println sc.&add(2,4);
		//sc.@heading = "Test";
		
		// Invoke a non-existent method:
		// Note invokeMethod is called for all methods whether they exist or not
		//sc.format(args);
		
		// Set a non-existent property:
		// Note setProperty is called for all properties whether they exist or not		
		sc.formatted = 3;
		
		// The following demonstrates a more elaborate call tree:
		// (uncomment the set of sc.heading above)
		sc.printItems(args);
		println(sc.heading + " complete (" + sc.count + " items)");		
	}
}

new WebSequenceDiagramDemo().demo();

