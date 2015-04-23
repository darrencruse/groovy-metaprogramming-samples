package com.mop.memoize

System.out.println "$configName configuring for $scriptName..."

Fibonacci.memoize "foo"
Fibonacci.memoize "fib"

// Include the tracing configuration script:
// evaluate(new File("TracingConfig.groovy"))
include "TracingConfig.groovy"