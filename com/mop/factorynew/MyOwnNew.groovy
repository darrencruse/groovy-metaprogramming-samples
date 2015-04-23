package com.mop.factorynew

class Bean implements GroovyInterceptable {
    String name

	def printName() { println "Name is $name" }
}

Bean.metaClass.invokeConstructor = { Object[] args -> println "In invokeConstructor: ($args)" }
Bean.metaClass.invokeMethod = { String name, Object[] args -> println "In invokeMethod: $name($args)" }
Bean b = new Bean(name: "fred")
b.printName()
println b.name
Bean.metaClass.static.new = { args -> println "new($args)" }
Bean b2 = Bean.new(name: "bambam")
Object.metaClass.static.new = { theClass, Object [] args -> 
    print "new $theClass("
    print args.join(", ")
    println ")"
	theClass.newInstance(args)
}
Bean b3 = Object.new(Bean, [name: "wilma"])
println b3.name