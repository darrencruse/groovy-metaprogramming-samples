package com.mop.tracing

/**
 * Trace the execution of constructors, methods, and properties using DelegatingMetaClass.
 * 
 * You can enable tracing for all instances of a class via <Class>.traceEnable(), or for a
 * single instance via instance.traceEnable().
 *
 * Note that when you enable at the instance level, you cannot intercept the constructor since
 * the object has already been created!
 *
 * @author Darren Cruse
 **/
// The following is arbitrary code, except we install
// the DelegatingMetaClass which implements our "advice"
// using the "traceEnable" methods from above:

class Fibonaccinator {
	
	private int targetNumber
	private int result
	
	Fibonaccinator(int t) {
		// note this is intercepted via setProperty - there is no field named "target"
		target = t
	}

	void init(int t) {		
		// But these are not intercepted - I guess these are
		// directly setting the fields above - not going through the
		// MOP as a performance optimization?
		targetNumber = t
		result = -1
	}

	void setTarget(int t) {
		init(t)
	}
	
	int getValue() {
		if(result < 0)
			result = fib(targetNumber)
		result
	}
		
    private int fib(int i) {
        if (i == 0 || i == 1) return 1;
        return fib(i-1)+fib(i-2);
    }
}

//GroovySystem.metaClassRegistry.metaClassCreationHandle = new MyMetaClassCreationHandle()
def fibber1 = new Fibonaccinator(2)
println fibber1.value


