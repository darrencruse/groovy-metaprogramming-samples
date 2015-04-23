package com.mop.memoize
class MemoizationDecorator {
	static void memoizeMethods(Class clazz, Set methods) {
		Map cache = [:]
		clazz.metaClass.invokeMethod = { String name, args ->
			
			def result
			
			// ignore methods that are not to be memoized:
			if (!methods.contains(name)) {
				def method = delegate.metaClass.getMetaMethod(name, args)
				result = method.invoke(delegate, args)				
			}
			else {
 
				// initialise the cache for this class/method if needed
				if (!cache[name]) cache[name] = [:]
				if (!cache[name][delegate]) cache[name][delegate] = [:]
			
				// is there already a memoized result?
				def key = args.collect{ it.hashCode().toString() }.join("-")
				result = cache[name][delegate][key]

				if (null == result) {
					// if there is no result, call the method
					def method = delegate.metaClass.getMetaMethod(name, args)
					if (method) result = method.invoke(delegate, args)
					
					// and store the result
					cache[name][delegate][key] = result
					System.out.println "Storing result ${clazz.name}.$name($args) = $result"					
				}
				else {
					System.out.println "Returned cached result ${clazz.name}.$name($args) = $result"
				}
			}
		
			return result
		}
	}
}
