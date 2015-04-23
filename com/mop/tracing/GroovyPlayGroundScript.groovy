package com.mop.tracing

class GroovyPlayGround {
	
	def presidentsMap = [Barack: "Obama", George: "Bush", Bill: "Clinton"]

	def getPresidents() { 
		presidentsMap 
	}
			
	def displayPresidents = {
		println "Presidents from Groovy:"
		presidents.each { firstName, lastName ->
			println "$firstName $lastName"
		}
	}
	
	static void main(args) {
				
		def pg = new GroovyPlayGround()
		if(args) {
			def lastName = pg.presidents[args[0]]
			if(lastName) {
				println "${args[0]} $lastName"				
			}
			else {
				println "Nobody by that name"
			}			
		}
		else {
			pg.displayPresidents()
		}
	}	
}

def tracer = new WebSequenceDiagrammer()
tracer.with {	
		
	traceEnable GroovyPlayGround

	// The file to write the web sequence diagram html to:
	traceFileName = "GroovyPlayGround.html"
	
	// The style to display the web sequence diagram with:
	// (see websequencediagrams.com for more options)
//	wsdStyle = "qsd"
//	wsdStyle = "rose"	
//	wsdStyle = "napkin"

	// The initial object for the sequence diagram:
	initialSender = "GroovyPlayGroundScript"

	// You can globally turn on/off tracing via the following:
	tracingOn = true

	// Display timing information?
	traceTiming = true

	// Trace exceptions? (e.g. methodMissing, propertyMissing)
	traceExceptions = true

	// Trace everything by default? 
	// (otherwise trace nothing by default and enable individually using regexes)
	traceByDefault = true

	// Take control of urn off some things we don't want to trace
	traceConstructor("*", true)	
	traceProperty(".*", "*", true)		
	traceMethod("*","asBoolean", false)	

	trace {
		GroovyPlayGround.main(null)		
	}
}