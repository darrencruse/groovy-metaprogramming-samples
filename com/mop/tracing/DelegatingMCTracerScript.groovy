package com.mop.tracing
class MPTracerDelegatingMetaClass extends groovy.lang.DelegatingMetaClass 
{
	
    MPTracerDelegatingMetaClass(final Class aclass) 
    {
        super(aclass)
        initialize()
    }

	Object invokeMethod(Object obj, String method, Object[] args) 
	{
	    System.out.println("            =>  ${method}(${args.join(', ')})");
				
        def result = super.invokeMethod(obj, method, args)

	    System.out.println("            <=  ${method}(${args.join(', ')} returned $result");

	    result
    }

	/**
	* Enable tracing for all instances of the specified class.
	* @return the original metaClass of the provided class is returned
	*/
    public static MetaClass delegateTracing(final Class aclass) {
		def originalMetaClass = aclass.metaClass;
		def tracingMetaClass = new MPTracerDelegatingMetaClass(aclass)
		aclass.metaClass = tracingMetaClass	
		originalMetaClass
	}	
}

public class MPTraced  {
    
    public int add(int a, int b) { a + b }     
   
}
    
// Set our tracer as the meta class for all instances of MPTraced:
MPTracerDelegatingMetaClass.delegateTracing(MPTraced)
MPTracerDelegatingMetaClass.delegateTracing(String)
   
MPTraced mpt = new MPTraced();
println mpt.add(2,4);

def name = "Fred"
println "Hello $name World" + "Darren"
