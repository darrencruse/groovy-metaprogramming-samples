package com.mop.tracing;
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
import groovy.lang.*;
import org.codehaus.groovy.runtime.metaclass.*;


import groovy.lang.MetaClassRegistry.MetaClassCreationHandle;
import java.lang.reflect.Constructor;
 
public class MyMetaClassCreationHandle extends MetaClassCreationHandle {
	
	protected MetaClass createNormalMetaClass(Class theClass,MetaClassRegistry registry) {
		String theClassName = theClass.getName();
		//System.out.println("createNormalMetaClass: " + theClassName);
		try {
			if(!theClassName.startsWith("java") &&
				!theClassName.startsWith("groovy") &&
				!theClassName.startsWith("org.codehaus.groovy") &&				
				!theClassName.endsWith("java.lang.Object;") &&
				!theClassName.equals("MyDelegateMetaClass")) 
			{
				System.out.println("createNormalMetaClass: instrumenting " + theClassName);
				MetaClass normalMetaClass = super.createNormalMetaClass(theClass, registry);
				return new MyDelegateMetaClass(normalMetaClass);
			}
			else {			
				return super.createNormalMetaClass(theClass, registry);
			}
		}
		catch (final Exception e) {
			throw new GroovyRuntimeException("Could not instantiate custom Metaclass for class: " + theClass.getName() + ". Reason: " + e, e);
		}		
	}
}

