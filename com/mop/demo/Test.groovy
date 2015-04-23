package com.mop.demo
class TestClass {
	String name
	
	TestClass(String theName) {
		name = theName
	}
}

def t = new TestClass("Fred")
println t.name

