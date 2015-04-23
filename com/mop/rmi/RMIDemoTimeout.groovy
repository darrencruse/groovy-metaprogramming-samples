package com.mop.rmi
#!/usr/bin/env groovy
import com.mop.factorynew.Adder;


RESTfulRMIClient.init()

println "Keep results in cache for 9 seconds"
Adder.remote "add", "localhost:8080", 9
// Note: Adder.remote above needs to be done *before* we create our instance of Adder below

println "Add 2,3 into cache"
adder = new Adder()
sum = adder.add(2,3)
println sum

println "Wait 5 seconds"
sleep 5000

println "Add 3,2..."
sum = adder.add(3,2)
println sum

println "Wait 5 more seconds"
sleep 5000

println "Now 2,3 should be refreshed, but 3,2 should come from cache"
sum = adder.add(2,3)
println sum
sum = adder.add(3,2)
println sum

println "Wait 5 more seconds"
sleep 5000

println "Now 3,2 should be refreshed, but 2,3 should come from cache" 
sum = adder.add(2,3)
println sum
sum = adder.add(3,2)
println sum