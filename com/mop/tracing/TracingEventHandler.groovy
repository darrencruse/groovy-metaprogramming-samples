package com.mop.tracing

public class TracingEventHandler
{	
	// Globally disable tracing with TracingEventHandler.tracingOn = false;
	// Re-enable tracing with TracingEventHandler.tracingOn = true;	
	public boolean tracingOn = true;
	
	public String traceFileName = null;
	
	public Writer traceWriter = null;
	
	// Assuming the global trace switch above is enabled, do we assume we trace
	// by default?  (otherwise we assume we *don't* trace by default)
	public boolean traceByDefault = true;	
	
	// Should timing be done and displayed in the trace?	
	public boolean traceTiming = false;	
			
	// Should exceptions displayed in the trace?	
	public boolean traceExceptions = true;
				
	// Enable/disable invidual methods from tracing (default is enabled):
	// E.g. TracingEventHandler.tracedMethods["<class>.<method>"] = false
	//      TracingEventHandler.tracedMethods["*.<method>"] = false
	//      TracingEventHandler.tracedMethods["*.*"] = false	
	public Map traceControlRegexes = [:];
	
	// The initial sender is the left most object in the sequence diagram
	public String initialSender = "script";	
				
	// The senders get pushed/popped as method calls come through:
	public List senderStack = [ ]	
			
	/**
	* Enable tracing for all instances of the specified class.
	* @return the original metaClass of the provided class is returned
	*/
    public MetaClass traceEnable(final Class aclass) {
		def originalMetaClass = aclass.metaClass;
		
		// You make an instance of the TracingDelegateMetaClass which will be a delegate for
		// the specified class, passing it ourself so it can send trace information back to us:
		def delegateMetaClass = new TracingDelegateMetaClass(aclass, this)
		
		// And set this as the meta class for the class it is to be a delegate for
		aclass.metaClass = delegateMetaClass	
		
		originalMetaClass
	}

	/**
	* Enable tracing for a single object
	* @return the original metaClass of the provided object is returned
	*/
    public MetaClass traceEnable(Object obj) {
		def originalMetaClass = obj.metaClass;
		
		// You make an instance of the TracingDelegateMetaClass which will be a delegate for
		// the specified class, passing it ourself so it can send trace information back to us:
		def delegateMetaClass = new TracingDelegateMetaClass(obj.class, this)
		
		// And set this as the meta class for the class it is to be a delegate for
		obj.metaClass = delegateMetaClass	
		
		originalMetaClass
	}

	public def trace(Closure c) {
		
		def outputStream;
		if(traceFileName && traceFileName != "stdout") {
			outputStream = new FileOutputStream(traceFileName)
		}		
		else {			
			outputStream = System.out;
		}
		
		outputStream.withWriter{ w ->
			traceWriter = w
			traceWriter << traceHeader()
			c()
			traceWriter << traceFooter()			
		}				
	}

	public void traceConstructor(String classRegex, Boolean on) {
		traceControlRegexes["constructor::${classRegex}::new"] = on;
	}

	public void traceMethod(String classRegex, String methodRegex, Boolean on) {
		traceControlRegexes["method::${classRegex}::${methodRegex}"] = on;
	}
			
	public void traceProperty(String classRegex, String propertyRegex, Boolean on) {
		traceControlRegexes["property::${classRegex}::${propertyRegex}"] = on;
	}	
	
	/**
	* Should this method/property/constructor call be traced?
	*/
	public def boolean shouldTrace(String traceType, String className, String methodOrProperty) {
					
		// Have they turned off all tracing?
		if(!tracingOn) {		
			return false;
		}
	
		// They can choose whether the default is to trace or not
		// to trace.
		def shouldTrace = traceByDefault;
		
		traceControlRegexes.each { matchSpecKey, tracingOn -> 
			String[] matchSpec = matchSpecKey.split("::")
			String traceTypeMatch = matchSpec[0]  // 'constructor', 'method', or 'property'			
			String classMatch = matchSpec[1]
			String methodPropMatch = matchSpec[2]
			
			if(traceType == traceTypeMatch)	{
				// If the class regex matches the class we're tracing:
				if("*" == classMatch || className ==~ classMatch)
				{
					// And if the method name regex matches the method or property name: 
					if("*" == methodPropMatch || methodOrProperty ==~ methodPropMatch)
					{
						// Then the associated value says if we trace or not:
						shouldTrace = tracingOn;
					}
				}
			}
		}			
				
		return shouldTrace;		
	}

	public def traceEvent(Map traceInfo) {
				
		// On exit we pop the stack:		
		if(traceInfo.type == "exit" || traceInfo.type == "exception") {			
			senderStack.pop()				
		}		

		// Note this is here so the senderStack can still be popped above
		// even when an exception occurs...
		if(!traceExceptions && "exception" == traceInfo.type) {
			return
		}
		
		// The sender is the last receiver we pushed on the stack:
		traceInfo.sender = senderStack.isEmpty() ? initialSender : senderStack[-1];
		
		// Invoke the provided trace closure:
		traceWriter << handleTraceEvent(traceInfo)
		
		// On entry this receiver becomes the new sender:
		if(traceInfo.type == "entry") {			
			senderStack.push(traceInfo.receiver)
			
			// And we return the start time in nanoseconds
			return System.nanoTime();
		}	
	}
	
	/**
	* The default trace results include no header or footer but you can
	* override these if you'd like to add such things.
	* @return a String to be written to the trace output stream 
	*/
	public def traceHeader = { "" }
	public def traceFooter = { "" }
	public def handleTraceEvent = { Map traceEvent ->
		
		def msg = "  " * senderStack.size()
		msg += "${traceEvent.type}: ${traceEvent.receiver}.${traceEvent.method}"						
		msg += traceEvent.args ? "(${traceEvent.args.join(', ')})" : "()"

		switch(traceEvent.type) {
			case "entry":
				msg += "\n";
				break;
							
			case "exit":			
				msg += traceEvent.returned ? " returned ${traceEvent.returned}": ""				
				if(traceTiming && traceEvent.startTime) {
					def duration = (System.nanoTime() - traceEvent.startTime) * 10E-10d;
					msg += " (time: ${String.format('%3.6f', duration)}s)\n"							
				}
				else {
					msg += "\n"
				}
				msg += "\n"																				
				break;
				
			case "exception":
				msg += "  ** ${traceEvent.what} exception **\n\n"												
				break;
		}
										
		msg		
	}		
}
