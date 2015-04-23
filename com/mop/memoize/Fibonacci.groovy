package com.mop.memoize
public class Fibonacci {
	
	public def fib(n)
	{
		Integer result 
		switch(n) {
			case 0..1:
				result = n
				break;
		
			default:
				// System.out.println "recursing to result = fib(${n-1}) + fib(${n-2})"		
				result = fib(n-1) + fib(n-2)
				break;
		}

		result
	}
	
}

