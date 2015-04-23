package com.mop.tracing
import com.mop.factorynew.NewMethods;

/**
 * Trace the execution of constructors, methods, and properties using DelegatingMetaClass.
 * 
 * The installation of DelegatingMetaClass happens implicitly for objects created via <Class>.new().
 *
 * @author Darren Cruse
 **/
public class NewMethods {

    public static List beforeNew = []        
    public static List afterNew = []

    public static void init() {

        Class.metaClass.static.new = { args ->        
            def instance
            def theClass = delegate
            beforeNew.each { it(theClass, args) }
            if(args != null)
                instance = theClass.newInstance(args)
            else
                instance = delegate.newInstance()                 
            afterNew.each { it(theClass, args, instance) }
            instance
        }        
        
    }
}

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

// The above are utility/helpers, and the following is initialization:

NewMethods.init()
 
// This advizes the class before constructing the instance (thus we also intercept invokeConstructor):
NewMethods.beforeNew << { theClass, args -> 
	System.out.println "${theClass.name}.new(${args ? (args.respondsTo('join') ? args.join(',') : args) : ''})"
    def delegateMetaClass = new MyDelegateMetaClass(theClass)
    
    theClass.metaClass = delegateMetaClass    
}

/*
// This advizes the constructed *instance* (so this alone can't intercept the constructor!):
NewMethods.afterNew << { theClass, args, newInstance -> 
	System.out.println "${theClass.name}.new(${args ? (args.respondsTo('join') ? args.join(',') : args) : ''})"
    def delegateMetaClass = new MyDelegateMetaClass(newInstance.class)
    
    newInstance.metaClass = delegateMetaClass    
}
*/

// And then now the following is arbitrary code where all the
// objects created via <Class>.new() are "advized"

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

def fibber
if(!args)
    fibber = Fibonaccinator.new(3)
else
    fibber = Fibonaccinator.new(new Integer(args[0]))
println fibber.value
