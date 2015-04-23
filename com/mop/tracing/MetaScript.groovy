#!/usr/bin/env groovy

import com.mop.tracing.TracingEventHandler;
import org.codehaus.groovy.control.CompilerConfiguration;
import com.mop.factorynew.NewMethods;
import com.mop.memoize.Memoization;
import com.mop.rmi.RESTfulRMIClient;

// Add support for "new" as a method on all classes.
NewMethods.init()

// Add support for memoization
Memoization.init()

// Add support for remoting methods to a RESTfulRMIServer
RESTfulRMIClient.init()

// A little help for including scripts from other scripts:
Script.metaClass.static.include = { otherScript -> delegate.evaluate(new File(otherScript)) }

def cli = new CliBuilder(usage: 'MetaScript.groovy -[chnt] <script.groovy>', header:'options:')
// Create the list of options.
cli.with {
    c longOpt: 'config', args: 1, argName: 'config', 
		'Configuration script - default is "<script>Config.groovy" otherwise "ScriptConfig.groovy"'
    h longOpt: 'help', 
		'Show usage information'
    n longOpt: 'no-config',   
		'Ignore configuration file (even if "<script>Config.groovy" exists)'
    t longOpt: 'trace', args: 1, argName: 'traceFile', 
		'"on"/"off" (to trace to <script>.<ext>), "stdout", or alternative trace file'
}

// Show usage text when n args given or -h or --help option is used.
def options = cli.parse(args)
if (!options || !options.arguments() || options.help) {
    cli.usage()	
    return
}

def scriptName = options.arguments()[0]
if(!scriptName.endsWith(".groovy")) {
	scriptName = "${scriptName}.groovy"
}
def scriptClassName = scriptName.replaceAll(/(.*)(\.groovy)$/, '$1')	

File configFile = null
def configName = options.config
if(configName) {	
	configFile = new File(configName)
} 
else {
	// Default is same as script name but ending "Config.groovy"
	configName = scriptName.replaceAll(/(.*)(\.groovy)$/, '$1Config$2')			
	configFile = new File(configName)
	if(!configFile.exists()) {
		System.out.println "No $configName configuration script..."
		
		// Otherwise generic default is "ScriptConfig.groovy"
		configName = "ScriptConfig.groovy"	
		configFile = new File(configName)
		if(!configFile.exists()) {
		System.out.println "No $configName configuration script..."			
			configName = ""
			configFile = null
		}		
	}
}

// Create the binding and the shell to execute against it:

// pass through the unprocessed args
Binding binding = new Binding(args)  
// as well as the parsed command line options
binding["options"] = options         
// the name of the script being run
binding["scriptName"] = scriptName   
// the name of the generated script class
binding["scriptClassName"] = scriptClassName
// "off" = no trace, "on" = default trace, otherwise custom name for trace file
binding["traceName"] = options.trace 

GroovyShell shell = new GroovyShell(binding);

// Load their main configuration script...
if(!options.n && configFile) {
	// Execute the tracing config which will populate the binding variables
	binding["configName"] = configName // the name of the config script 
	shell.evaluate(configFile);
}

/*// Load the trace configuration script...
if(options.trace != "off") {
	// the name of the config script 
	def tracingConfigName = "TracingConfig.groovy"
	binding["configName"] = tracingConfigName // the name of the tracing config 	
	shell.evaluate(new File(tracingConfigName))
}*/

// Rebind args omitting the options that MetaScript.groovy uses:
binding["args"] = options.arguments().tail()

// See if the config created a "tracer":
Object tracer = null
try { tracer = binding["tracer"] } catch(Exception e) { }

if(tracer && options.trace != "off") {
	// Trace the script against the tracing configuration bindings:
    Script script = shell.parse(new GroovyCodeSource(new File(scriptName), 
								CompilerConfiguration.DEFAULT.getSourceEncoding()));
    script.setBinding(binding);
	tracer.traceEnable(script)
	tracer.trace {
        script.run();		
	}	
}
else {	
	// The configuration script did not create a valid "tracer" to use:
	shell.evaluate(new File(scriptName));	
}
