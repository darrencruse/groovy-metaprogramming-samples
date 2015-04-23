package com.mop.tracing


public class SimpleClass {

    String heading = ""
    int count = 0
	
	public int add(int a, int b) { a + b } 	
	
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

// Can also do:  SimpleMOPTracingDemo extends SimpleTracer 
public class SimpleMOPTracingDemo {
	public demo() {
		def args = (String[])["a", "b", "c"]
		SimpleClass sc = new SimpleClass()
		
		// invoke a simple method returning a value:
		def sum1 = sc.add(2,4)

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
		System.out.println(sc.heading + " complete (" + sc.count + " items)")		
	}
}

// Set our tracer as the meta class for:
SimpleTracingMetaClass.with {	
	traceEnable SimpleMOPTracingDemo
	traceEnable SimpleClass

	// The file to write the web sequence diagram html to:
	traceFileName = "SimpleTracingScript.html"

//	wsdStyle = "qsd"
//	wsdStyle = "rose"	
//	wsdStyle = "napkin"

		
	// The initial object for the sequence diagram:
	initialSender = "SimpleTracingScript"

	// You can globally turn on/off tracing via the following:
	tracingOn = true

	// You can globally turn on/off tracing via the following:
	traceTiming = true

	// Trace exceptions? (e.g. methodMissing, propertyMissing)
	traceExceptions = true

	// Trace by default? (otherwise enable individually using regexes)
	traceByDefault = true

	// Turn off some things we don't want to trace
	traceConstructor("*", true)	
	traceProperty("*", "*", true)	
//	traceProperty("Simple.*", "heading", true)		
	traceMethod("*","asBoolean", false)
//	traceMethod("Simple.*","print.*", true)	
}

SimpleTracingMetaClass.metaClass.traceHeader = {
"""<html>
	<head>
		<title>${SimpleTracingMetaClass.initialSender} Sequence Diagram</title>
	</head>
	<body>
		<div class=wsd wsd_style="${SimpleTracingMetaClass.wsdStyle}"><pre>
"""
}

SimpleTracingMetaClass.metaClass.traceFooter = { 
"""		</pre></div><script type="text/javascript" src="http://www.websequencediagrams.com/service.js"></script>
	</body>
</html>
"""
}

SimpleTracingMetaClass.metaClass.traceMsg = { Map info ->

	def msg = ""			
			
	switch(info.type) {
		case "entry":
				msg += "${info.sender}->${info.receiver}: "
				msg += info.method ? info.method : ""

				if("invokeConstructor" == info.what) {
					msg += " ${info.receiver}" // (receiver class name = name of constructor)
				}						
				msg += info.args ? "(${info.args.join(', ')})" : "()"
				msg += "\n"								
				break;
				
		case "exit":
//System.out.println "In exit! ${info.sender != info.receiver}"				
				// A constructor call activates the class in the sequence diagram
				if("invokeConstructor" == info.what) {
					msg += "activate ${info.receiver}\n"
				}		

				if(SimpleTracingMetaClass.traceTiming && info.startTime) {
					def duration = (System.nanoTime() - info.startTime) * 10E-9d;
					msg += "note left of ${info.receiver}: Time: ${String.format('%3.6f', duration)}s\n"							
				}
		
				// Don't include a return arrow for functions within the same object
				if(info.sender != info.receiver) {
					msg += "${info.receiver}-->${info.sender}: "						
					msg += info.returned ? info.returned : ""
					msg += "\n"																				
				}
															
				break;
				
		case "exception":
				msg += "note right of ${info.receiver}: ${info.what} exception\n"							
				break;
	}
										
	msg		
}

def tracingDemo = new SimpleMOPTracingDemo()
tracingDemo.demo()
SimpleTracingMetaClass.closeTraceFile()