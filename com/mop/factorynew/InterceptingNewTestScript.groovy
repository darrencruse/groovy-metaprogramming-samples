package com.mop.factorynew
import com.mop.memoize.Foo;

/*import SimpleClass

//Object.metaClass.new = { delegate.class.newInstance() }
SimpleClass.metaClass.constructor = { 
	def instance = null
    println "default constructor ${delegate.name}";
	Class c = Class.forName(delegate.name, true, this.getClass().getClassLoader())
	MetaMethod mm = c.metaClass.getMetaMethod("newInstance", null)
	if(mm) {
		instance = mm.invoke(delegate, null)
	}
	else {
		println "Could not find the newInstance MetaMethod"
	}
	instance
}

//Foo.metaClass.constructor = { Object[] args -> 
//    println "constructor ${delegate.name}, $args!"; 
//    delegate.&newInstance(args) 
//}

//Class c = Class.forName("SimpleClass", true, this.getClass().getClassLoader())
//def bean = c.newInstance()
def bean = new SimpleClass()
bean.heading = "Darren"
println bean*/

/*class Foo { } 

Foo.metaClass.constructor = { 
    println "default constructor ${delegate.name}";
    Foo.newInstance()
}

def bar = new Foo()*/

class Foo { } 

Object.metaClass.constructor = { 
    println "default constructor ${delegate.name}";
    delegate.newInstance()
}

def bar = new Foo()