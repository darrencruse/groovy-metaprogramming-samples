package com.mop.factorynew
public class NewMethods {

	// These happen before and after *any* class that 
	// gets created using the "new method" approach:
	public static List beforeNew = []		
	public static List afterNew = []

	// These are the default implementations (closures) that have 
	// been specified for certain classes/interfaces/named objects/etc.
	private static Map creators = [:]

	static class CreatorInfo {
		Object creator    // can be a closure or a "prototype" object
		Boolean singleton // if false and creator is an object not a closure, 
						  // the object will be cloned each time new() is called.		
	}

	public static void init() {
		//System.out.println "Installing new method handling..."

		// Install a closure to act as the creator when <Class>.new() is called
		Class.metaClass.static.creator = { closure ->
			System.out.println "creator ${delegate.name}"; 
			creators[delegate.name] = new CreatorInfo(creator: closure)
		}
		
		// Install a closure to act as the creator when "objectname".new() is called
		String.metaClass.static.creator = { closure ->
			System.out.println "creator ${delegate}"; 
			creators[delegate] = new CreatorInfo(creator: closure)
		}

		// Install a prototype to be cloned when <Class>.new() is called
		Class.metaClass.static.prototype = { prototype ->
			System.out.println "prototype ${delegate.name}"; 
			creators[delegate.name] = new CreatorInfo(creator: prototype, singleton: false)
		}
		
		// Install a prototype to be cloned when "objectname".new() is called
		String.metaClass.static.prototype = { prototype ->
			System.out.println "prototype ${delegate}"; 
			creators[delegate] = new CreatorInfo(creator: prototype, singleton: false)
		}

		// Install a singleton to be returned when <Class>.new() is called
		Class.metaClass.static.singleton = { singleton ->
			System.out.println "singleton ${delegate.name}"; 
			creators[delegate.name] = new CreatorInfo(creator: singleton, singleton: true)
		}
		
		// Install a singleton to be returned when "objectname".new() is called
		String.metaClass.static.singleton = { singleton ->
			System.out.println "singleton ${delegate}"; 
			creators[delegate] = new CreatorInfo(creator: singleton, singleton: true)
		}

		// Note this is not on an individual class but on all classes:
		Class.metaClass.static.new = { args ->
			NewMethods.createInstance(delegate.name, delegate, args)
		}		
	
		// This is for creating objects using a named "factory" i.e. "<name>".new(args)
		String.metaClass.static.new = { args ->
			NewMethods.createInstance(delegate, String, args)			
		}
	}
	
	// Helper method creates instances using registered creators (otherwise newInstance) 
	// for classes/interfaces/(string) names
	private static createInstance(String factoryName, Class theClass, args) {

		def instance
		
		beforeNew.each { 
			if(it instanceof Closure) {
				it(theClass, args)
			}
		}
		
		CreatorInfo creatorInfo = creators[factoryName]
		if(creatorInfo) {
			// If it's a closure the closure will return the 
			// object to be returned, and should accept the 
			// same args they passed to <Class>.new():
			if(creatorInfo.creator instanceof Closure) {
				if(args) {
					instance = creatorInfo.creator(args) 
				}
				else {
					instance = creatorInfo.creator()
				}					
			}
			else {
				if(creatorInfo.singleton) {
					// It's actually the singleton to return
					instance = creatorInfo.creator
				}
				else {
					// It's a prototype to be copied each time .new() is called
					// Use the MOP to clone the object - thanks to:
					// 	http://snipplr.com/view/8477/cloning-an-object-in-groovy-using-mop/
					def writeableProps = theClass.metaClass.getProperties().findAll(){it.getSetter()!=null}
					instance = writeableProps.inject(theClass.newInstance()){ copy, metaProp ->
									metaProp.setProperty(copy,metaProp.getProperty(creatorInfo.creator))
				            		copy
				    }
				}
			}
		}
		else {
			// No creator registered - use newInstance:
			if(args) {
				instance = theClass.newInstance(args) 
			}
			else {				
				instance = theClass.newInstance()
			}				
		}

		afterNew.each { 				
			if(it instanceof Closure) {					
				it(theClass, args, instance)
			}
		}				
		instance
	}	
}
