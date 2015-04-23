package com.mop.rmi
import com.mop.factorynew.Adder;

System.out.println "$configName configuring for $scriptName..."

Adder.remote "add", "localhost:8080", 0

// Include the tracing configuration script:
include "TracingConfig.groovy"