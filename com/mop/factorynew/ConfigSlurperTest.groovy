package com.mop.factorynew
//import NewMethods
//NewMethods.init()

//ExpandoMetaClass.enableGlobally()

// This is for configuring the properties of an instance (e.g. a singleton)
Object.metaClass.config = { Closure c ->
	c.delegate = delegate
	c.resolveStrategy = Closure.DELEGATE_FIRST	
	c()	
	delegate
}

// This is for configuring the properties of all instances of a specified class
Class.metaClass.config = { Closure c ->
System.out.println "In class config"
	// Create an instance of this class
	def prototype = delegate.newInstance()
	// Configure it using their closure
	prototype.config(c)
	// Make it the prototype for future instances of this class
	delegate.prototype prototype	
	delegate
}

def cfg = new ConfigSlurper().parse(new File(".ConfigSlurperTest.groovy").toURL())

// This is working to make the root level stuff in the config
// feel like it's "global" inside ConfigClient.  I don't think
// this the right answer in the end though - globals are worrisome
// things...  Maybe it should have iterated over the root level
// objects from the config and exposed only those that were not
// ConfigObject as individual properties off Object.  i.e. These
// are the singleton objects created in the config.  Everything
// else is truly ConfigSlurper style config and maybe should be 
// accessed explicitly using "config."(?)  In order to not 
// pollute the global namespace?

// Make the configuration available globally as "config."
Object.metaClass.config = cfg

// And make singletons available globally as well:
cfg.each { k, v -> 
	if(!(v instanceof ConfigObject)) {
System.out.println "Making $k available globally"		
		Object.metaClass."$k" = v
	}
}

/*
Object.metaClass.getProperty = { String propName ->	
	def meta = Object.metaClass.getMetaProperty(propName)
	if (meta) {
		meta.getProperty(delegate)
	} else {
		cfg."$propName"
	}
}
*/

Object.metaClass.globalVar = "wilma"
def client = new ConfigClient()
client.doSomething()


