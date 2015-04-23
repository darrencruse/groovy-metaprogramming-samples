package com.mop.tracing

public class WebSequenceDiagrammer extends TracingEventHandler {

	/**
	* wsdStyle sets the style of Web Sequence Diagram - see
	* websequencediagrams.com for details:
	* 	default
    * 	earth
    * 	modern-blue
    * 	mscgen
    * 	omegapple
    * 	qsd
    * 	rose
    * 	roundgreen
    * 	napkin
	*/
	public static String wsdStyle = "omegapple"

    WebSequenceDiagrammer() 
    {
		super.traceHeader = wsdHeader;
		super.traceFooter = wsdFooter;
		super.handleTraceEvent = wsdTraceEvent;
    }

	public def wsdHeader = {
	"""<html>
	<head>
		<title>${initialSender} Sequence Diagram</title>
	</head>
	<body>
		<div class=wsd wsd_style="${wsdStyle}"><pre>
	"""
	}

	public def wsdFooter = { 
	"""		</pre></div><script type="text/javascript" src="http://www.websequencediagrams.com/service.js"></script>
		</body>
	</html>
	"""
	}

	public def wsdTraceEvent = { Map traceEvent ->

		def msg = ""			
			
		switch(traceEvent.type) {
			case "entry":
					msg += "${traceEvent.sender}->${traceEvent.receiver}: "
					msg += traceEvent.method ?: ""

					if("invokeConstructor" == traceEvent.what) {
						msg += " ${traceEvent.receiver}" // (receiver class name = name of constructor)
					}						
					msg += traceEvent.args ? "(${traceEvent.args.join(', ')})" : "()"
					msg += "\n"								
					break;
				
			case "exit":			
					// A constructor call activates the class in the sequence diagram
					if("invokeConstructor" == traceEvent.what) {
						msg += "activate ${traceEvent.receiver}\n"
					}		

					if(traceTiming && traceEvent.startTime) {
						def duration = (System.nanoTime() - traceEvent.startTime) * 10E-10d;
						msg += "note left of ${traceEvent.receiver}: Time: ${String.format('%3.6f', duration)}s\n"							
					}
		
					// Don't include a return arrow for functions within the same object
					if(traceEvent.sender != traceEvent.receiver) {
						msg += "${traceEvent.receiver}-->${traceEvent.sender}: "						
						msg += traceEvent.returned ? traceEvent.returned : ""
						msg += "\n"																				
					}
															
					break;
				
			case "exception":
					msg += "note right of ${traceEvent.receiver}: ${traceEvent.what} exception\n"							
					break;
		}
										
		msg		
	}

}
