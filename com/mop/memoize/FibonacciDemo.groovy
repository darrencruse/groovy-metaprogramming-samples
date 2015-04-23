package com.mop.memoize
#!/usr/bin/env groovy
import com.mop.factorynew.NewMethods;


NewMethods.init()

Integer i = Integer.parseInt(args[0])
Fibonacci f = Fibonacci.new()

def start = System.nanoTime()
println "fib($i) = ${f.fib(i)}"
def duration = (System.nanoTime() - start) * 10E-10d;
println "(time: ${String.format('%3.6f', duration)}s)"
