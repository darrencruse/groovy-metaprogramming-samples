package com.mop.tracing

public class TracingDelegateMetaClass extends groovy.lang.DelegatingMetaClass 
{
	// Multiple delegating meta classes log trace messages through a common tracer:
	protected TracingEventHandler tracer = null;

    protected TracingDelegateMetaClass(final Class aclass, TracingEventHandler tracerInstance) 
    {
        super(aclass)
        initialize()

	 	tracer = tracerInstance;	
    }
	
	/**
	* Forward the traceInfo along with the name of the class we're delegating for over
	* to the provided TracingEventHandler
	*/		
	public def traceEvent(Map traceInfo) {		
		// The receiving class is the one we're delegating for:
		traceInfo.receiver = delegate.theClass.name
		
		// Pass the trace info to the TracingEventHandler for tracing
		tracer.traceEvent traceInfo
	}
	
	@Override
	Object invokeMethod(Object object, String methodName, Object[] args) {

		if(!tracer.shouldTrace('method', delegate.theClass.name, methodName)) {
			return super.invokeMethod(object, methodName, args)	
		}	
		
		def result = null
		try {			
			def start = traceEvent(what: "invokeMethod", 
						type:  "entry",			
						method: methodName, 
						args: args)	
											
			// Allow the DelegatingMetaClass to invoke the real method
			// Note:  Stuff above is the equivalent of AOP "before advice"					
        	result = super.invokeMethod(object, methodName, args)

			// Stuff below is the equivalent of AOP "after advice"			
			traceEvent what: "invokeMethod", 
						type:  "exit",			
						method: methodName, 
						args: args, 
						returned: result, 
						startTime: start
		}
		catch(MissingMethodException e) {
			traceEvent what: "methodMissing", 
						type: "exception",			
						method: methodName, 
						args: args
		}
				
		// We must return what the method returned:
		result
	}

	@Override
	Object invokeConstructor(Object[] args) {
	
		if(!tracer.shouldTrace('constructor', delegate.theClass.name,"new")) {				
			return super.invokeConstructor(args);
		}		
				
		def start = traceEvent(what: "invokeConstructor",
		 			type: "entry",		
					method: "new",		
					args: args)
						
		def newObject = super.invokeConstructor(args);
		
		traceEvent what: "invokeConstructor",
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
			!tracer.shouldTrace('property', delegate.theClass.name,propName)) {
			return super.getProperty(object, propName);	
		}
					
        Object propValue = null
		try 
		{
			def start = traceEvent(what: "getProperty", 
						type:  "entry",
						method: propertyMethodName("get", propName))			
						
			propValue = super.getProperty(object, propName);
			
			traceEvent what: "getProperty",
						type: "exit",			 
						method: propertyMethodName("get", propName), 
						returned: propValue, 
						startTime: start						
		}
		catch(MissingPropertyException mpe) {
			traceEvent what: "propertyMissing",
						type: "exception",				 
						method: propertyMethodName("get", propName)						
		}
		
        propValue;            
    }

	@Override
    void setProperty(Object object, String propName, Object newValue) { 
		
		if(!tracer.shouldTrace('property', delegate.theClass.name,propName)) {
			super.setProperty(object, propName, newValue);
			return
		}
			
		try 
		{ 
			def start = traceEvent(what: "setProperty", 
						type: "entry", 
						method: propertyMethodName("set", propName),
						args: [newValue])
										   
        	super.setProperty(object, propName, newValue);

			traceEvent what: "setProperty",
						type: "exit",			  
						method: propertyMethodName("set", propName),
						args: [newValue],
						startTime: start
		}
		catch(MissingPropertyException mpe) {
			traceEvent what: "propertyMissing",
						type: "exception",				 
						method: propertyMethodName("set", propName),
						args: [newValue]										
		}
    } 

 	private String propertyMethodName(String methodType, String propName) {
		methodType + propName[0].toUpperCase() + propName[1..-1]
	}	
}
