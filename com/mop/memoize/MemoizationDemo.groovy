package com.mop.memoize
#!/usr/bin/env groovy

Memoization.init()

class Foo {

	def bar(x) { 
		println "In Foo.bar($x)" 
		x
	}

}

println this.class.name

Foo.memoize "bar"

Foo foo = new Foo()
	
def a = foo.bar(1)
def b = foo.bar(2)
def c = foo.bar(1)
def d = foo.bar(3)

println "b = ${foo.bar(2)}"
