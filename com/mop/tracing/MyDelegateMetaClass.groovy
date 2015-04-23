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
    protected MyDelegateMetaClass(MetaClass theDelegate) 
    {
        super(theDelegate)
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
