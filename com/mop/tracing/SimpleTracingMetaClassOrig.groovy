package com.mop.tracing

class SimpleTracingMetaClass extends groovy.lang.DelegatingMetaClass 
{
	
    SimpleTracingMetaClass(final Class aclass) 
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
    public static MetaClass trace(final Class aclass) {
		def originalMetaClass = aclass.metaClass;
		def tracingMetaClass = new SimpleTracingMetaClass(aclass)
		aclass.metaClass = tracingMetaClass	
		originalMetaClass
	}

}
