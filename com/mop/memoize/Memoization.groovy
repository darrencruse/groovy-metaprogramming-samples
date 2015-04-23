package com.mop.memoize
/**
*  Memoization Class
*
*  This is a somewhat rearranged version of MemoizationDecorator by Jörn Dinkla 
*  (see "http://blog.dinkla.net/?tag=groovy").  Thanks Jörn! :)
*
*  @author Darren Cruse and Jörn Dinkla
*  
*/
public class Memoization {
	
	private static Set methods = [] as Set		
	private static Map cache = [:]	
	
	public static void init() {	
							
		Class.metaClass.static.memoize = { functionName ->		

			methods << "${delegate.name}.${functionName}"
			// System.out.println "Memoized: $methods"
			
			delegate.metaClass.invokeMethod = { String name, args ->
				// System.out.println "In memoizing invokeMethod ${delegate.class.name}.${name}(${args.join(', ')})"
				def result

				// ignore methods that are not to be memoized:
				if (!methods.contains("${delegate.class.name}.${name}")) {				
					def method = delegate.metaClass.getMetaMethod(name, args)
					if(method) {						
						result = method.invoke(delegate, args)									
					}
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
						if (method) {
							// Note:  it appears if I *dont* have tracing on, recursive calls are
							//        not being itercepted back through this invokeMethod call.
							//        Maybe the instance level delegation that tracing does 
							//        helps? (i.e. where it does super.invokeMethod?) 
							result = method.invoke(delegate, args)						
						}
						
						// and store the result
						cache[name][delegate][key] = result
						// System.out.println "Storing result ${delegate.class.name}.$name(${args.join(', ')}) = $result"					
					}
					else {
						// System.out.println "Returned cached result ${delegate.class.name}.$name(${args.join(', ')}) = $result"
					}
				}

				return result
			}	
		}
	}
}