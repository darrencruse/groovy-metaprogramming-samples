package com.mop.demo
String.metaClass.shout = { delegate.toUpperCase(); }
println "Groovy".shout()

String.metaClass.toUpperCase = { delegate.toLowerCase(); }
println "Groovy".toUpperCase()

