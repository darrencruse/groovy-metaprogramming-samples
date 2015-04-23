#!/usr/bin/env groovy
import com.mop.factorynew.NewMethods;
import com.mop.tracing.TracingDemo

NewMethods.init()

def tracingDemo = TracingDemo.new()
tracingDemo.go()
