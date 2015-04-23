package com.mop.tracing
import com.mop.factorynew.NewMethods;
import com.mop.memoize.Fibonacci;

/**
 * Trace the execution of methods (only) using DelegatingMetaClass.
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
            System.out.println("?  No such method as $methodName(${args.join(',')})")        
        }
                
        // We must return what the method returned:
        result
    }

    // Omitted (for brevity) similar to the above for 
    //  invokeConstructor, getProperty, setProperty
    
}

// The above are utility/helpers, and the following is initialization:

NewMethods.init()
   
NewMethods.beforeNew << { theClass, args -> 
	System.out.println "${theClass.name}.new(${args ? args.join(',') : ''})"
    def delegateMetaClass = new MyDelegateMetaClass(theClass)
    
    theClass.metaClass = delegateMetaClass    
}


// And then now the following is arbitrary code where all the
// objects created via <Class>.new() are "advized"

class Fibonacci {
    int fib(int i) {
        if (i == 0 || i == 1) return 1;
        return fib(i-1)+fib(i-2);
    }
}

def fibber = Fibonacci.new()
if(!args)
    println fibber.fib(5)
else
    println fibber.fib(new Integer(args[0]))