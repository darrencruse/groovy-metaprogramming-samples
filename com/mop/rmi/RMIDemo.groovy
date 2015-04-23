#!/usr/bin/env groovy
package com.mop.rmi
import com.mop.factorynew.Adder;
import com.mop.factorynew.Bean;


RESTfulRMIClient.init()

Adder.remote "add", "localhost:8080", 0
// Note: Adder.remote above needs to be done *before* we create our instance of Adder below

adder = new Adder()
sum = adder.add(7,6)
println sum

Bean.remote "getTheBean", "localhost:8080", 0

bean = new Bean()
println bean.getTheBean()


