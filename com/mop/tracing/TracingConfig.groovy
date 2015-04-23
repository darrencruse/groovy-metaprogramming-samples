package com.mop.tracing
import com.mop.factorynew.NewMethods;

//import TracingDemo
//import SimpleClass
//import Fibonacci

System.out.println "TracingConfig.groovy configuring for $scriptName..."

if(options.trace != "off") {
	def traceTo = options.trace 
	if(!traceTo || traceTo == "on") {
		// use the default
		traceTo = scriptName.replaceAll(/(.*)(\.groovy)$/, '$1.html')
	}
	System.out.println "Tracing to $traceTo"
	
	// Don't use "def" here - we need tracer visible in our output bindings!
	tracer = new WebSequenceDiagrammer()
	//tracer = new TracingEventHandler()

	// Enable tracing on any class used with <class>.new()
// NEED TO TEST WHAT HAPPENS IF YOU DO <interface>.new USING THE NEW STUFF.  OR A SINGLETON?	
	NewMethods.beforeNew << { theClass, args -> tracer.traceEnable(theClass) }	
	
	// Enable tracing on any instance created via <class>.new()
	// Note:  Using afterNew misses the chance the trace the actual contruction of the instance.	
	//NewMethods.afterNew << { theClass, args, instance -> tracer.traceEnable(instance) }	

	tracer.with {	

	// You can also enable tracing directly by class (an alternative to using beforeNew/afterNew) 
	//traceEnable TracingDemo
	//traceEnable SimpleClass
	//  traceEnable Fibonacci
	
		// The file to write the web sequence diagram html to:
		traceFileName = traceTo

		// The style to display the web sequence diagram with:
		// (see websequencediagrams.com for more options)
	//	wsdStyle = "qsd"
		wsdStyle = "rose"	
	//	wsdStyle = "napkin"

		// The initial object for the sequence diagram:
		initialSender = scriptClassName

		// You can globally turn on/off tracing via the following:
		tracingOn = true

		// Display timing information?
		traceTiming = true

		// Trace exceptions? (e.g. methodMissing, propertyMissing)
		traceExceptions = true

		// Trace everything by default? 
		// (otherwise trace nothing by default and enable individually using regexes)
		traceByDefault = true

		// Take control of urn of some things we don't want to trace
		traceConstructor("*", true)	
		traceProperty(".*", "*", true)	
		traceProperty("*", "class", false)
		traceProperty("*", "metaClass", false)				
		traceProperty(/Simple.*/, "heading", true)
		traceProperty(scriptClassName, "out", false)				
		traceMethod(scriptClassName,"run", false)			
		traceMethod("*","asBoolean", false)	
		traceMethod(/.*/,/print.*/, true)	
	}	
}






