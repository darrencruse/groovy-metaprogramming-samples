package com.mop.tracing

import groovy.lang.GroovyInterceptable;

public class SimpleTracer implements GroovyInterceptable {
	
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
		
		def result = this.metaClass.invokeMethod(this, name, args);
		
		// Stuff below is the equivalent of AOP "after advice"
		
		this.&trace what: "invokeMethod", name: name, args: args, returned: result, startTime: start		
		
		// We must return what the method returned:
		result
	}	
	
    Object getProperty(String propName) {		
        Object propValue = this.metaClass.getProperty(this, propName);
		this.&trace what: "getProperty", msg: "$propValue = $propName"		
        propValue;            
    }

    void setProperty(String propName, Object newValue) {
		this.&trace what: "setProperty", msg: "$propName = $newValue"    
        this.metaClass.setProperty(this, propName, newValue);    
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

public class SimpleClass extends SimpleTracer {

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

// Can also do:  SimpleMOPTracingDemo extends SimpleTracer 
public class SimpleMOPTracingDemo {
	public demo() {
		def args = (String[])["a", "b", "c"]
		SimpleClass sc = new SimpleClass();
		
		// invoke a simple method returning a value:
		println sc.add(2,4);

		// set a simple property:
		sc.heading = "Test";
		
		// You can invoke methods directly with ".&" and set properties directly with ".@"
		println sc.&add(2,4);
		sc.@heading = "Test";
		
		// Invoke a non-existent method:
		// Note invokeMethod is called for all methods whether they exist or not
		sc.format(args);
		
		// Set a non-existent property:
		// Note setProperty is called for all properties whether they exist or not		
		sc.formatted = 3;
		
		// The following demonstrates a more elaborate call tree:
		// (uncomment the set of sc.heading above)
		sc.printItems(args);
		println(sc.heading + " complete (" + sc.count + " items)");		
	}
}

new SimpleMOPTracingDemo().demo();
