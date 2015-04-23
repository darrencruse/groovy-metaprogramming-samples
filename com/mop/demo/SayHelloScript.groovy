package com.mop.demo
import com.mop.factorynew.Bean;

#!/usr/bin/env groovy
class Bean {
    def sayHello = { howMany ->
        if(howMany > 0) {
            println "hello"
            sayHello(howMany - 1)
        }
    }
}

Bean b = new Bean()
b.sayHello(3)