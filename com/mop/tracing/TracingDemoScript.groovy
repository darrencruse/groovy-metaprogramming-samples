package com.mop.tracing

public class SimpleClass {

    String heading = ""
    int count = 0
	
	public int add(int a, int b) { a + b } 	

	public closure = { "this is a closure!" }
	
    private void printItem(String item) {
        System.out.println("item " + count + ": " + item)
        count++
    }

    public void printItems(String[] items) {
        System.out.println(heading)
        for(String item: items) {
            printItem(item)
        }
    } 
}

public class TracingDemo {
	public go() {
		def args = (String[])["a", "b", "c"]
		SimpleClass sc = new SimpleClass()
		
		// invoke a simple method returning a value:
		def sum1 = sc.add(2,4)

		// invoke a simple closure returning a value:		
		def str = sc.closure();
		
		// set a simple property:
		sc.heading = "Test"
		
		// You can invoke methods directly with ".&" and set properties directly with ".@"
		def sum2 = sc.&add(2,4)
		sc.@heading = "Test"
		
		// Invoke a non-existent method:
		// Note invokeMethod is called for all methods whether they exist or not
		sc.format(args)
		
		// Set a non-existent property:
		// Note setProperty is called for all properties whether they exist or not		
		sc.formatted = 3
		
		// The following demonstrates a more elaborate call tree:
		// (uncomment the set of sc.heading above)
		sc.printItems(args)
		System.out.println(sc.heading + " complete (" + sc.count + " items)\n")		
	}
}
	
def tracer = new WebSequenceDiagrammer()
tracer.with {	
		
	traceEnable TracingDemo
	traceEnable SimpleClass

	// The file to write the web sequence diagram html to:
	traceFileName = "TracingDemoScript.html"
	
	// The style to display the web sequence diagram with:
	// (see websequencediagrams.com for more options)
//	wsdStyle = "qsd"
//	wsdStyle = "rose"	
//	wsdStyle = "napkin"

	// The initial object for the sequence diagram:
	initialSender = "TracingDemoScript"

	// You can globally turn on/off tracing via the following:
	tracingOn = true

	// Display timing information?
	traceTiming = false

	// Trace exceptions? (e.g. methodMissing, propertyMissing)
	traceExceptions = true

	// Trace everything by default? 
	// (otherwise trace nothing by default and enable individually using regexes)
	traceByDefault = true

	// Take control of urn off some things we don't want to trace
	traceConstructor("*", true)	
	traceProperty(".*", "*", true)	
	traceProperty(/Simple.*/, "heading", true)		
	traceMethod("*","asBoolean", false)	
	traceMethod(/Simple.*/,/print.*/, true)	

	trace {
		def tracingDemo = new TracingDemo()
		tracingDemo.go()		
	}
}

