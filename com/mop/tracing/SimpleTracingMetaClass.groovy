package com.mop.tracing
class SimpleTracingMetaClass extends groovy.lang.DelegatingMetaClass 
{
	// Globally disable tracing with SimpleTracingMetaClass.tracingOn = false;
	// Re-enable tracing with SimpleTracingMetaClass.tracingOn = true;	
	public static boolean tracingOn = true;
	
	public static String traceFileName = null
	
	public static File traceFile = null;
	
	// Assuming the global trace switch above is enabled, do we assume we trace
	// by default?  (otherwise we assume we *don't* trace by default)
	public static boolean traceByDefault = true;	
	
	// Should timing be done and displayed in the trace?	
	public static boolean traceTiming = false;	
			
	// Should exceptions displayed in the trace?	
	public static boolean traceExceptions = true;
				
	// Enable/disable invidual methods from tracing (default is enabled):
	// E.g. SimpleTracingMetaClass.tracedMethods["<class>.<method>"] = false
	//      SimpleTracingMetaClass.tracedMethods["*.<method>"] = false
	//      SimpleTracingMetaClass.tracedMethods["*.*"] = false	
	private static Map traceControlRegexes = [:];
	
	// The initial sender is the left most object in the sequence diagram
	public static String initialSender = "script";	
				
	// The senders get pushed/popped as method calls come through:
	private static List senderStack = [ ]	
		
    SimpleTracingMetaClass(final Class aclass) 
    {
        super(aclass)
        initialize()

		// Note:  The calls to delegate.theClass.name cause an infinite loop without:
		traceProperty("*", "name", false);		
    }

	/**
	* Enable tracing for all instances of the specified class.
	* @return the original metaClass of the provided class is returned
	*/
    public static MetaClass traceEnable(final Class aclass) {
		def originalMetaClass = aclass.metaClass;
		def tracingMetaClass = new SimpleTracingMetaClass(aclass)
		aclass.metaClass = tracingMetaClass	
		originalMetaClass
	}

	public static def traceConstructor(String classRegex, boolean on) {
		traceControlRegexes["constructor::${classRegex}::new"] = on;
	}

	public static def traceMethod(String classRegex, String methodRegex, boolean on) {
		traceControlRegexes["method::${classRegex}::${methodRegex}"] = on;
	}
	
	public static def traceProperty(String classRegex, String propertyRegex, boolean on) {
		traceControlRegexes["property::${classRegex}::${propertyRegex}"] = on;
	}	
	
	def traceMsg = { Map info ->
		// The line format can vary:	
		def symbol = info.exit || info.returned || info.startTime ? "<==" : "==>"			
		def line = "            ${symbol}  ${info.what}:  "
		
		if(info.msg) {
			line += info.msg
		}
		else {
			line += info.method ? info.method : ""		
			line += info.args ? "(${info.args.join(', ')})" : "()"
		}
//		line += info.startTime ? " took ${(System.nanoTime() - info.startTime) * 10E-9d} seconds" : ""				
		line += info.returned ? " : ${info.returned}" : ""	
					
		// Important to use System.out.println not println here! (can you guess why?)		
		System.out.println line
		
		// We return the time this tracing was done:
		System.nanoTime();		
	}

	/**
	* The default trace results include no header or footer but you can
	* override these if you'd like to add such things.
	* @return a String to be written to the trace output stream 
	*/
	public static traceHeader = { "" }
	public static traceFooter = { "" }

	/**
	* wsdStyle belongs in a WSD specific class once I'm done -
	* can be any of the following:
	* default
    * earth
    * modern-blue
    * mscgen
    * omegapple
    * qsd
    * rose
    * roundgreen
    * napkin
	*/
	public static String wsdStyle = "omegapple"

	private def traceEvent(Map traceInfo) {
		
		// The receiving class is the one we're delegating for:
		traceInfo.receiver = delegate.theClass.name
				
		// On exit we pop the stack:		
		if(traceInfo.type == "exit" || traceInfo.type == "exception") {			
			senderStack.pop()				
		}		

		// Note this is here so the senderStack can still be popped above...
		if(!traceExceptions && "exception" == traceInfo.type) {
			return
		}
		
		// The sender is the last receiver we pushed on the stack:
		traceInfo.sender = senderStack.isEmpty() ? initialSender : senderStack[-1];
		
		// Invoke the provided trace closure:
		def msg = this.&traceMsg(traceInfo)
		writeTraceMsg(msg)
		
		// On entry this receiver becomes the new sender:
		if(traceInfo.type == "entry") {			
			senderStack.push(traceInfo.receiver)
			
			// And we return the start time in nanoseconds
			return System.nanoTime();
		}	
	}

	private def writeTraceMsg(String traceMsg) {
		
		if(traceFileName != null) {
			if(!traceFile) {
				traceFile = new File(traceFileName)
				traceFile.setText(this.&traceHeader())
			}
			
			if(traceFile) { 			
				traceFile.append(traceMsg)
			}
		}
		else {
			System.out.print traceMsg
		}
	}

	public static def closeTraceFile() {
		
		if(traceFileName != null) {
			if(!traceFile) {
				traceFile = new File(traceFileName)
			}
			
			if(traceFile) { 			
				traceFile.append(this.&traceFooter())
			}
		}
	}

	/**
	* Should this method/property/constructor call be traced?
	*/
	private def boolean shouldTrace(String traceType, String className, String methodOrProperty) {
					
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

	@Override
	Object invokeMethod(Object object, String methodName, Object[] args) {

		if(!this.&shouldTrace('method', delegate.theClass.name, methodName)) {
			return super.invokeMethod(object, methodName, args)	
		}	
		
		def result = null
		try {			
			def start = this.&traceEvent(what: "invokeMethod", 
						type:  "entry",			
						method: methodName, 
						args: args)	
											
			// Allow the DelegatingMetaClass to invoke the real method
			// Note:  Stuff above is the equivalent of AOP "before advice"					
        	result = super.invokeMethod(object, methodName, args)

			// Stuff below is the equivalent of AOP "after advice"			
			this.&traceEvent what: "invokeMethod", 
						type:  "exit",			
						method: methodName, 
						args: args, 
						returned: result, 
						startTime: start
		}
		catch(MissingMethodException e) {
			this.&traceEvent what: "methodMissing", 
						type: "exception",			
						method: methodName, 
						args: args
		}
				
		// We must return what the method returned:
		result
	}

	@Override
	Object invokeConstructor(Object[] args) {
		
		if(!this.&shouldTrace('constructor', delegate.theClass.name,"new")) {
			return super.invokeConstructor(args);
		}		
				
		def start = this.&traceEvent(what: "invokeConstructor",
		 			type: "entry",		
					method: "new",		
					args: args)
						
		def newObject = super.invokeConstructor(args);
		
		this.&traceEvent what: "invokeConstructor",
		 			type: "exit",		
					method: "new",		
					args: args, 
					returned: newObject, 
					startTime: start
				
		newObject
	}	
	
	@Override		
    Object getProperty(Object object, String propName) {
	
		if("name" == propName ||
			!this.&shouldTrace('property', delegate.theClass.name,propName)) {
			return super.getProperty(object, propName);	
		}
					
        Object propValue = null
		try 
		{
			def start = this.&traceEvent(what: "getProperty", 
						type:  "entry",
						method: propertyMethodName("get", propName))			
						
			propValue = super.getProperty(object, propName);
			
			this.&traceEvent what: "getProperty",
						type: "exit",			 
						method: propertyMethodName("get", propName), 
						returned: propValue, 
						startTime: start						
		}
		catch(MissingPropertyException mpe) {
			this.&traceEvent what: "propertyMissing",
						type: "exception",				 
						method: propertyMethodName("get", propName)						
		}
		
        propValue;            
    }

	@Override
    void setProperty(Object object, String propName, Object newValue) { 
		
		if(!this.&shouldTrace('property', delegate.theClass.name,propName)) {
			super.setProperty(object, propName, newValue);
			return
		}
			
		try 
		{ 
			def start = this.&traceEvent(what: "setProperty", 
						type: "entry", 
						method: propertyMethodName("set", propName),
						args: [newValue])
										   
        	super.setProperty(object, propName, newValue);

			this.&traceEvent what: "setProperty",
						type: "exit",			  
						method: propertyMethodName("set", propName),
						args: [newValue],
						startTime: start
		}
		catch(MissingPropertyException mpe) {
			this.&traceEvent what: "propertyMissing",
						type: "exception",				 
						method: propertyMethodName("set", propName),
						args: [newValue]										
		}
    } 

 	private String propertyMethodName(String methodType, String propName) {
		methodType + propName[0].toUpperCase() + propName[1..-1]
	}	
}
