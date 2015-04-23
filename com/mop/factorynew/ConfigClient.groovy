package com.mop.factorynew

class ConfigClient {
	def doSomething() {

println globalVar

		println config.log4j.rootLogger
		println config.log4j.additivity.aBean

		println aLogger
		println theLogger.theBean
		println TheLogger.instance.theBean
		
		println Bean.new()		
	}	
}
