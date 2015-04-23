package com.mop.tracing

/**
 * Trace the execution of constructors, methods, and properties using DelegatingMetaClass.
 * 
 * You can enable tracing for all instances of a class via <Class>.traceEnable(), or for a
 * single instance via instance.traceEnable().
 *
 * Note that when you enable at the instance level, you cannot intercept the constructor since
 * the object has already been created!
 *
 * @author Darren Cruse
 **/

public class MyDelegateMetaClass extends DelegatingMetaClass 
{
    protected MyDelegateMetaClass(final Class aclass) 
    {
        super(aclass)
        initialize()    
    }

	@Override
	Object invokeConstructor(Object[] args) {
		System.out.println "new ${theClass.name}(${args ? args.join(',') : ''})"					
		super.invokeConstructor(args)
	}  
      
    @Override
    Object invokeMethod(Object object, String methodName, Object[] args) {
        
        def result = null
        try {
            // whatever happens here is "before advice"
            System.out.println("-> Calling:  $methodName(${args.join(',')})")
                                            
            // Allow the DelegatingMetaClass to invoke the real method                
            result = super.invokeMethod(object, methodName, args)

            // whatever happens here is "after advice"
            System.out.println("<- $methodName(${args.join(',')}) returned $result")
        }
        catch(MissingMethodException e) {
            System.out.println("?  No such method $methodName(${args.join(',')})")        
        }
                
        // We must return what the method returned:
        result
    }

	@Override		
    Object getProperty(Object object, String propName) {
		
		if("name" == propName) {
			return super.getProperty(object, propName);	
		}		
					
        Object propValue = null
		try 
		{
            // whatever happens here is "before advice"
            System.out.println("-> Getting:  $propName")			
						
			propValue = super.getProperty(object, propName);
			
            // whatever happens here is "after advice"
            System.out.println("<- Got $propValue for $propName")						
		}
		catch(MissingPropertyException mpe) {
            System.out.println("?  No such property $propName") 								
		}
		
        propValue;            
    }
 
	@Override
    void setProperty(Object object, String propName, Object newValue) { 
					
		try 
		{ 
            // whatever happens here is "before advice"
            System.out.println("-> Setting:  $propName to $newValue")
													   
        	super.setProperty(object, propName, newValue);

            // whatever happens here is "after advice"
            System.out.println("<- Set $propName to $newValue")
		}
		catch(MissingPropertyException mpe) {
            System.out.println("?  No such property $propName")										
		}
    }    
}
 
// This advizes the class before constructing the instance (thus we also intercept invokeConstructor):
Class.metaClass.traceEnable = {  
    def delegateMetaClass = new MyDelegateMetaClass(delegate)
    delegate.metaClass = delegateMetaClass    
}

// This advizes the constructed *instance* (so this alone can't intercept the constructor!):
Object.metaClass.traceEnable = {
    def delegateMetaClass = new MyDelegateMetaClass(delegate.class)
    delegate.metaClass = delegateMetaClass    
}

// The following is arbitrary code, except we install
// the DelegatingMetaClass which implements our "advice"
// using the "traceEnable" methods from above:

class Fibonaccinator {
	
	private int targetNumber
	private int result
	
	Fibonaccinator(int t) {
		// note this is intercepted via setProperty - there is no field named "target"
		target = t
	}

	void init(int t) {		
		// But these are not intercepted - I guess these are
		// directly setting the fields above - not going through the
		// MOP as a performance optimization?
		targetNumber = t
		result = -1
	}

	void setTarget(int t) {
		init(t)
	}
	
	int getValue() {
		if(result < 0)
			result = fib(targetNumber)
		result
	}
		
    private int fib(int i) {
        if (i == 0 || i == 1) return 1;
        return fib(i-1)+fib(i-2);
    }
}

println "Advize a single (already constructed) instance:"
def fibber1 = new Fibonaccinator(2)
fibber1.traceEnable()
println fibber1.value

println "Advize all instances of a class (note constructor is now advized):"
Fibonaccinator.traceEnable()
def fibber2 = new Fibonaccinator(3)
println fibber2.value
