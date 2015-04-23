package com.mop.tracing
import com.mop.factorynew.NewMethods;
import com.mop.bean.SimpleClass;


public class TracingDemo {
	
	public go() {
		NewMethods.init()
		def args = (String[])["a", "b", "c"]
		SimpleClass sc = SimpleClass.new()
		//SimpleJavaClass sc = SimpleJavaClass.new()
				
		// invoke a simple method returning a value:
		def sum1 = sc.add(2,4)

		// invoke a simple closure returning a value:		
//		def str = sc.closure();
		
		// set a simple property:
		sc.heading = "Test"
		
		// You can invoke methods directly with ".&" and set properties directly with ".@"
		def sum2 = sc.&add(2,4)
		sc.@heading = "Test"
		
		// Invoke a non-existent method:
		// Note invokeMethod is called for all methods whether they exist or not
		//sc.format(args)
		
		// Set a non-existent property:
		// Note setProperty is called for all properties whether they exist or not		
		//sc.formatted = 3
		
		// The following demonstrates a more elaborate call tree:
		// (uncomment the set of sc.heading above)
		sc.printItems(args)
		System.out.println(sc.heading + " complete (" + sc.count + " items)\n")		
	}
}

