#!/usr/bin/env groovy

package com.mop.rmi

/**
 * RESTfulRMIServer
 *
 * RESTfulRMIServer is an enhancement to the SimpleWebServer written 
 * by Jeremy Rayner which allows you to evaluate groovy method calls 
 * and scripts.
 *
 * Run using
 *    groovy -l 80 RESTfulRMIServer.groovy
 * 
 *       (where 80 is the port to listen for requests upon) 
 *
 * You can invoke groovy code RESTfully using the following:
 * 
 *   http://<hostname>/r2/com/company/math/Fibonacci/fib/1 
 *
 * Invokes the method "fib" on the "class com.company.math.Fibonacci"
 * passing the single argument 1.
 *
 *   http://<hostname>/r2/com/company/math/Adder/add?a=2&b=4 
 *	 
 * Invokes the method "add" on the class "com.company.math.Fibonacci"
 * passing a Map of [a: 1, b: 2].
 * 
 *  http://<hostname>/script/com/company/math/FibonacciScript/-trace/fib.log/8
 *
 * Invokes a script just as if the following were entered on the command line:
 *
 * ./com/company/math/FibonacciScript -trace fib.log 8
 * 
 * @author <a href="mailto:darren.cruse@gmail.com">Darren Cruse</a>
 */
import java.io.File
import com.thoughtworks.xstream.*
import com.thoughtworks.xstream.io.xml.DomDriver
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver; 

if (init) { 
    headers = [:] 
    binaryTypes = ["gif","jpg","png"]          
    mimeTypes = [
        "css" : "text/css",         
        "gif" : "image/gif",
        "htm" : "text/html",         
        "html": "text/html",         
        "jpg" : "image/jpeg",         
        "png" : "image/png",
        "txt" : "text/plain",
		"xml" : "application/xml",
		"json" : "application/json"
    ]                                 
}

// parse the request
if (line.toLowerCase().startsWith("get")) {
    content = line.tokenize()[1]
} else {
    h = line.tokenize(":")
    headers[h[0]] = h[1]
}

// all done, now process request
if (line.size() == 0) {
    processRequest()
    return "success"
}

// ------------------------

def processRequest() {
		
	// "/r2/" = remote method invocation
	if(content.startsWith("/r2/")) {
		processGroovyRMIRequest()
	}
	// "/script/" = remote script invocation
	else if(content.startsWith("/script/")) {
		processGroovyScriptRequest()
	}
	// otherwise we act as web server for files
	else {
		processFileRequest()
	}
}

@Grab(group='com.thoughtworks.xstream', module='xstream', version='[1.3.1,)')
def processGroovyRMIRequest() {
	
	def pathAndQueryString = content.trim().tokenize("?")
	def path = pathAndQueryString[0]
	def queryString = null
	if(pathAndQueryString.size() > 1) {
		queryString = pathAndQueryString[1]
	}
	
	def parts = path.tokenize("/")	
	
	// throw away the beginning:
	parts = parts.minus("r2")
	//println "parts=" + parts

	// The class is the first one with an upper case letter
	def classNameIndex = parts.findIndexOf{ part -> 
												part.any{ c -> Character.isUpperCase(c as Character) } 
											}
	//println "classNameIndex=" + classNameIndex
		
	def className = parts.subList(0,classNameIndex+1).join(".")
	//println "className=" + className	
	def methodName = parts[classNameIndex+1]
	
	String format = "txt"
    String ext = methodName.substring(methodName.lastIndexOf(".") + 1)	
	if(ext == "xml" || ext == "json" || ext == "txt") {
		format = ext
		//println "format = $format"			
		methodName = methodName.substring(0,methodName.lastIndexOf("."))		
	}
	
	printHeaders(format)
		
	//println "methodName=" + methodName	
	def methodArgs = parts.subList(classNameIndex+2,parts.size())
	//println "methodArgs=" + methodArgs
	Map methodArgsMap = [:]
	if(queryString) {
		def queryParams = queryString.tokenize("&")
		queryParams.each{ param ->
			def paramParts = param.tokenize("=")
			if(paramParts.size() == 2)
			methodArgsMap[paramParts[0]] = paramParts[1]
		}
	}
	
	def scriptText = formatGroovyRMIScript(className, methodName, methodArgs, methodArgsMap)
	//println scriptText 	
		
	// Capture stdout and stderr so we can send *everything* back to the browser
	PrintStream oldErr = System.err;
	PrintStream oldOut = System.out;
	ByteArrayOutputStream errByteStream = new ByteArrayOutputStream();
	PrintStream errPrintStream = new PrintStream(errByteStream);		
	ByteArrayOutputStream outByteStream = new ByteArrayOutputStream();
	PrintStream outPrintStream = new PrintStream(outByteStream);				
	System.setErr(errPrintStream);
	System.setOut(outPrintStream);
	
	// formattedResult is what we'll return to the browser	
	String outcome = "success"		
	Object result = null	
	try {
		GroovyShell shell = new GroovyShell();
		result = shell.evaluate(scriptText);	
	}
	catch(Exception e) {
		// Note here that stderr is still redirected so printStackTrace is captured
		e.printStackTrace()	
			
		// Likewise this will go to stdout
		println e.getMessage()	
		
		outcome = "failure"	
	}

	System.setErr(oldErr);
	System.setOut(oldOut);

	println formatResult(outcome, format, result, outByteStream, errByteStream)		
}

String formatResult(String outcome, String format, Object result, ByteArrayOutputStream outByteStream, ByteArrayOutputStream errByteStream) {
	
	// println "$outcome = $result" 
	String outStr = outByteStream.toString()
	//if(outStr) println outStr
	String errStr = errByteStream.toString()		
	//if(errStr) println errStr
	
	String formattedResult
	switch(format) {
		case "txt":
			formattedResult = formatTextResult(outcome, result, outStr, errStr)		
			break;
		
		case "xml":
			formattedResult = formatXmlResult(outcome, result, outStr, errStr)
			break;
			
		case "json":
			formattedResult = formatJsonResult(outcome, result, outStr, errStr)
			break;			
	}

	formattedResult
}

String formatTextResult(String outcome, Object result, String outStr, String errStr) {
// I put result last because it feels better for what the coded to appear in the browser
// above what it returned.  Seems like there should be optional query params or something 
// to suppress stdout & stderr though.  Maybe if the url is "/r2/silent/..." ?
"${outStr}${errStr}${result}"
}

String formatXmlResult(String outcome, Object result, String outStr, String errStr) {
String outcomeTag = outcome != "success" ? "<outcome>$outcome</outcome>" : ""	
String resultTag = "<result>${toXML(result)}</result>"
String outTag = outStr ? "<stdout>$outStr</stdout>" : ""
String errTag = errStr ? "<stderr>$errStr</stderr>" : ""
"<r2>${outcomeTag}${resultTag}${outTag}${errTag}</r2>"
}

String formatJsonResult(String outcome, Object result, String outStr, String errStr) {
String outcomeTag = outcome != "success" ? "\"outcome\": \"$outcome\"" : ""	
String resultTag = "\"result\": ${toJSON(result).trim()}"
String outTag = outStr ? "\"stdout\": \"$outStr\"" : ""
String errTag = errStr ? "\"stderr\": \"$errStr\"" : ""
def results = [outcomeTag, resultTag, outTag, errTag]
def resultsStr = results.findAll { it }.join(",\n")
"""{ "r2": {
${resultsStr}
} }"""
}

def toXML(Object obj) {
	def xstream = new XStream(new DomDriver())
	xstream.classLoader = getClass().classLoader			
	xstream.toXML(obj)
}

def toJSON(Object obj) {
	def xstream = new XStream(new JsonHierarchicalStreamDriver())
	xstream.setMode(XStream.NO_REFERENCES)
	xstream.toXML(obj)
}

// As currently coded it's either positional args or Map (named) args (not both!)
def formatGroovyRMIScript(className, methodName, methodArgs, methodArgsMap) {

	// It's either named args (from query string) or position args (from path)
	def argsString = ""
	if(methodArgsMap && !methodArgsMap.isEmpty()) {
		argsString = quoteNonIntegers(methodArgsMap).toString()[1..-2]
	}
	else {
		argsString = quoteNonIntegers(methodArgs).join(', ')
	}

	// Return the script as follows:
"""
	def obj = new ${className}()
	obj.${methodName}(${argsString})
"""	
}

def List quoteNonIntegers(List listOfStrings) {
	listOfStrings.collect{ it.isInteger() ? it : "\"${it}\"" }
}

def Map quoteNonIntegers(Map mapOfStrings) {
	Map result = [:]
	mapOfStrings.each{ k, v ->
		result[k] = v.isInteger() ? v : "\"${v}\"" 
	}
	result
}

def processGroovyScriptRequest() {
		
	def parts = content.tokenize("/")	
	
	// throw away the beginning:
	parts = parts.minus("script")
	//println "parts=" + parts

	// The script name is the first one with an upper case letter
	def scriptNameIndex = parts.findIndexOf{ part -> 
												part.any{ c -> Character.isUpperCase(c as Character) } 
											}
	//println "scriptNameIndex=" + scriptNameIndex
		
	def scriptName = "./" + parts.subList(0,scriptNameIndex+1).join("/")
	if(!scriptName.endsWith(".groovy")) scriptName += ".groovy"
 	//println "scriptName=" + scriptName	
	
	def commandLineArgs = parts.subList(scriptNameIndex+1,parts.size())
	//println "commandLineArgs=" + commandLineArgs
	
	println getHeaders("text/plain") 		
	try {
		PrintStream oldErr = System.err;
		PrintStream oldOut = System.out;
		
		ByteArrayOutputStream bytesErr = new ByteArrayOutputStream();
		PrintStream newErrStream = new PrintStream(bytesErr);		
		ByteArrayOutputStream bytesOut = new ByteArrayOutputStream();
		PrintStream newOutStream = new PrintStream(bytesOut);				
		System.setErr(newErrStream);
		System.setOut(newOutStream);
		Binding binding = new Binding()
		binding.args = commandLineArgs as String[]	
		GroovyShell shell = new GroovyShell(binding);
		shell.evaluate(new File(scriptName));
		def outStr = bytesOut.toString()
		if(outStr) println outStr
		def errStr = bytesErr.toString()		
		if(errStr) println errStr	
					
		System.setErr(oldErr);
		System.setOut(oldOut);	
	}
	catch(Exception e) {
		println e.getMessage()
	}
		
}

def processFileRequest() {
	
    if (content.indexOf("..") < 0) { //simplistic security
	
        // simple file browser rooted from current dir
		def name = "." + content
		try {
	        f = new File("." + content)
	        if (f.isDirectory()) {
	            printDirectoryListing(f)
	        } else {
	            ext = content.substring(content.lastIndexOf(".") + 1)
	            printHeaders(ext)          
                      
	            if (binaryTypes.contains(ext)) {
	                socket.outputStream.write(f.readBytes())
	            } else {
	                println(f.text)
	            }
	        }
		}
		catch(FileNotFoundException fnf) {
			println "No such file: $name"
		}
    }
}

def printDirectoryListing(f) {
    println getHeaders("text/html")          
    println "<html><head></head><body>"
    for (i in f.list().toList().sort()) {
        if ("/" == content) { content = "" } // special case for root document
        println "<a href='${content}/${i}'>${i}</a><br>"
    }
    println "</body></html>"
}

def getHeaders(mimeType) {
"""HTTP/1.0 200 OK
Content-Type: ${mimeType}

"""          
}

def printHeaders(ext) {
	println getHeaders(mimeTypes.get(ext,"text/plain"))
}
