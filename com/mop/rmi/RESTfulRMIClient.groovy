package com.mop.rmi
//package com.darren.cruse.r2

/**
*  RESTfulRMIClient Class
*
*  This class adds a "remote" method on classes so that you can cause the invocation of
*  methods to be remoted e.g.:
*     EndOfDayJob.remote "subscriptions", "subscriptionhost:8080", 300
*
* Indicates that if the method "subscriptions" is invoked on class EndOfDayJob, it
* should be forwarded to the RESTfulRMIServer running on port 8080 of host
* "subscriptionhost", with the returned value cached for 300 seconds.
*
* THE FOLLOWING IS TBD:
*
* If you'd like any and all "missing method" invocations on a class to forward,
* use "missing" in place of the method name e.g.:
*
*     EndOfDayJob.remote "missing", "subscriptionhost:8080"
*
* Lastly, if you'd like all methods on the class to forward omit the method name,
* e.g.
*     EndOfDayJob.remote "subscriptionhost:8080"
*
* Note that this last case is most useful if your local "EndOfDayJob" class is
* just a stub with no methods, e.g. (an alternate to the above):
*
*     class EndOfDayJob {
*        EndOfDayJob() { remote "subscriptionhost:8080"	}
*     }
*
* If the class is not a stub, be aware all method calls against the class will be 
* forwarded to the other server - whether they exist on the local class or not!
*
*  @author Darren Cruse
*/
import com.thoughtworks.xstream.*
import com.thoughtworks.xstream.io.xml.DomDriver

public class RESTfulRMIClient {
	
	private static Map methods = [:]		
	
	static class RemoteMethodInfo {
		String className
		String methodName
		String hostName
		Integer cacheTimeout
		Map cache
	}
	
	static class RemoteMethodResult {
		Boolean error
		Object result
		Date   generatedAt
	}
		
	public static void init() {	
							
		Class.metaClass.static.remote = { String methodName, String hostName, Integer cacheTimeoutSeconds ->		

			// Note:  A current shortcoming is we don't account for overloading of methods
			//        where the same method name may exist with different types or arity.			 
			methods["${delegate.name}.${methodName}"] = new RemoteMethodInfo(
				className: delegate.name,
				methodName: methodName,
				hostName: hostName,
				cacheTimeout: cacheTimeoutSeconds,
				cache: [:]);
								
			System.out.println "Remoted: $methods"
			
			delegate.metaClass.invokeMethod = { String name, args ->
				System.out.println "In remoting invokeMethod ${delegate.class.name}.${name}(${args.join(', ')})"

				// ignore methods that are not to be remoted:
				def methodInfo = methods["${delegate.class.name}.${name}"]
				if (!methodInfo) {				
					def method = delegate.metaClass.getMetaMethod(name, args)
					if(method) {						
						return method.invoke(delegate, args)									
					}
				}
				else {
					RemoteMethodResult methodResult = null					
					if(methodInfo.cacheTimeout > 0) {

						def now = new Date();
							
						// is there already a cached result?
						def key = args.collect{ it.hashCode().toString() }.join("-")
						methodResult = methodInfo.cache[key]
						if(methodResult) {
							// Is it fresh enough?

							// Compute expiration time:
							def expirationTime
							def calendar = Calendar.instance
							calendar.with {
							  time = methodResult.generatedAt
							  add SECOND, methodInfo.cacheTimeout
							  expirationTime = time
							}
							
							// Has this result expired?
							if(now.after(expirationTime)) {								
								methodResult = null // yup
							}
						}
						
						if (methodResult == null) {
							// Call the remote method
							methodResult = callRemoteMethod(methodInfo.hostName, 
														methodInfo.className,
														name,
														args)
																				
							// and store the result
							if(!methodResult.error) {							
								System.out.println "Caching remote result ${delegate.class.name}.${name}(${args.join(', ')}) = ${methodResult.result}"								
								methodInfo.cache[key] = methodResult
							}												
						}
						else {
							// Just return the cached result
							System.out.println "Returned cached result ${delegate.class.name}.${name}(${args.join(', ')}) = ${methodResult.result}"
						}
					}
					else {
						
						// We're not caching - call the remote method and return what it returns
						// Note the explicit call out to RESTfulRMIClient here - at first I unthinkingly had
						// callRemoteMethod calling back through this very invokeMethod!
						methodResult = RESTfulRMIClient.callRemoteMethod(methodInfo.hostName, 
													methodInfo.className,
													name,
													args)						
						System.out.println "Returning remote result ${delegate.class.name}.${name}(${args.join(', ')}) = ${methodResult.result}"						
					}
					
					return methodResult.result ?: null					
				}
				
			}	
		}
	}
	
	@Grab(group='com.thoughtworks.xstream', module='xstream', version='[1.3.1,)')	
	private static RemoteMethodResult callRemoteMethod(hostName, className, methodName, args) {
		String remoteMethodUrl = "http://${hostName}/r2/${className}/${methodName}.xml/${args.join('/')}"
		System.out.println "Requesting remote method url:  ${remoteMethodUrl}"
		
		def r2Xml = remoteMethodUrl.toURL().text.trim()
		//System.out.println "Got back xml:  ${r2Xml}"
				
		// Decided to use text parsing here assuming it's a bit faster
		// esp. since I'm going to run the result through XStream anyway...
		
		// An example response is like:		
		// <r2>
		//    <result>
		//      <Bean>
		//          <firstName>Darren</firstName>
		//          <lastName>Cruse</lastName>
		//       </Bean>
		//    </result>
		//    <stdout>Returning a bean</stdout>
		// </r2>		
			
		//def r2parts = ( r2Xml =~ /.*<result>(.*)<\/result>.*/ )
		//def methodResultXml = r2Xml[0]
		
		def methodResultXml = getTagContent("result", r2Xml)
				
		System.out.println "Got back result xml:  ${methodResultXml}"
						
		def xstream = new XStream(new DomDriver())
		//this actually breaks the line below:  xstream.classLoader = getClass().classLoader		
		def methodResult = xstream.fromXML(methodResultXml)						
		//System.out.println "Got back:  ${methodResult}"
				
		return new RemoteMethodResult(error: false, result: methodResult, generatedAt:  new Date())		
	}
	
	private static String getTagContent(String tagName, String xml) {
		String result = null
		int startTag = xml.indexOf("<${tagName}>")
		if(startTag >= 0) {
			int endTag = xml.indexOf("</${tagName}>")
			if(endTag >= 0) {
				result = xml.substring(startTag + "<${tagName}>".length(), endTag)
			}			
		}
		else {
			int emptyTag = xml.indexOf("<${tagName}/>")
			if(emptyTag >= 0) {
				result = ""
			}
		}
	}
}

