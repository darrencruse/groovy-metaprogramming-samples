package com.mop.tracing

public class WebSequenceDiagrammer {
	
	def trace(Map info) {
		// The line format can vary:	
		def symbol = info.exiting || info.returned || info.startTime ? "<==" : "==>"			
		def line = "            ${symbol}  ${info.what}:  "
		
		if(info.msg) {
			line += info.msg
		}
		else {
			line += info.name ? info.name : ""		
			line += info.args ? "(${info.args.join(', ')})" : ""
			line += info.startTime ? " took ${(System.nanoTime() - info.startTime) * 10E-9d} seconds" : ""
			line += info.returned ? " returned ${info.returned}" : ""			
		}
		
		// Important to use System.out.println not println here! (can you guess why?)		
		System.out.println line
		
		// We return the time this tracing was done:
		System.nanoTime();		
	}	
	
	Object invokeMethod(String name, Object args) {
		
		def start = this.&trace(what: "invokeMethod", name: name, args: args)
		
		// Invoke the real method
		// Note:  Stuff above is the equivalent of AOP "before advice"

//		def result = this.&add(*args)
//		def result = WebSequenceDiagrammedClass.metaClass.invokeMethod(this, name, args)		
//		def result = this.metaClass.invokeMethod(this, name, args)
//		def result = this.&"$name"(*args)
		def metaMethod = this.metaClass.getMetaMethod(name, args)
		def result = null
		if(metaMethod)
		{
this.&println "Found $name()!"	+ metaMethod.class.name
//this.&println this.&getClass().&getName()			
			result = metaMethod.invoke(this, args)
		}
		else {
this.&println "Could not find $name()!"			
		}
		
		// Stuff below is the equivalent of AOP "after advice"
		
		this.&trace what: "invokeMethod", name: name, args: args, returned: result, startTime: start		
		
		// We must return what the method returned:
		result
	}	
	
    Object getProperty(String propName) {		
        Object propValue = this.&getProperty(this, propName);
		this.&trace what: "getProperty", msg: "$propValue = $propName"		
        propValue;            
    }

    void setProperty(String propName, Object newValue) {
		this.&trace what: "setProperty", msg: "$propName = $newValue"    
        this.&setProperty(this, propName, newValue);    
    }
			
    Object methodMissing(String name, Object args) {
		this.&trace what: "methodMissing", name: name, args: args
    }
	
	// This is on a get
    def propertyMissing(String name) { 
		this.&trace what: "propertyMissing", name: name
    }   
	
	// This is on a set
    def propertyMissing(String name, value) { 
		this.&trace what: "propertyMissing", msg: "$name = $value"
    }
	
	// The MOP api does support invokeConstructor -
	// but I think it can only be set at the meta class level
	// (i.e. this was not being invoked I should remove this)
	Object invokeConstructor(Object args) {
		System.out.println("            =>  invokeConstructor:  ${this.class.name}(${args.toString()[1..-2]})");
		def start = System.nanoTime();
		def newObject = this.metaClass.invokeConstructor(args);
		def elapsed = System.nanoTime() - start;
		System.out.println("            <=  (${this.class.name}(${args.toString()[1..-2]}) took ${elapsed * 10E-9d} seconds)");
		newObject
	}	
}
