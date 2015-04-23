package com.mop.demo
import org.codehaus.groovy.runtime.StackTraceUtils;

//Closure closure = { a, b ->
//	a + b;
//}  
//fred = closure(3, 5)
//println fred


class A {
	public def wilma = { a, b ->
		return a + b / 0
	}
}

class B {
	
	def a = new A();
	
	def fred()  {
		a.wilma(3, 5);
	}
}

try {
	b = new B();
	b.fred();
} catch(Exception e) {
	throw StackTraceUtils.sanitize(e);
}