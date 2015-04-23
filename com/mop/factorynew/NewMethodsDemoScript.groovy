package com.mop.factorynew
/**
This script demonstrates the use of new methods as an alternative to the new operator.

Through the use of metaprogramming, the new methods play the role of configurable factories
as demonstrated below.

The rationale behind the thinking can be found in the article "Java's New Considered Harmful":

	http://www.drdobbs.com/184405016

Note that the article pre-dates the popularity of Inversion of Control containers like Spring.

The example below is meant to illustrate that you can get *some* of the loose coupling of IOC
containers very simply in a language like groovy in ways that are easier for non-IOC-aware
developers to pick up (for starters they just have to switch from "new XXX()" to "XXX.new()").

The initial motive for developing these features was so that all constructed objects in a
program could be proxied (wrapped) in order to add tracing features.  It did not appear that
Groovy's MOP invokeConstructor support let you easily do this for any/all objects in your
program (it appears you'd have to do the proxying one class at a time)

Lastly, note that in this script the "metaprogramming"/"configuring" code is intermingled
with the using code.  Of course in a real application you'd want to separate the two so
the code that does the creator/singleton/prototype setups (and of course all the calls to
the actual new operator as opposed to the new method) was separated from the rest of the 
system. 

That way you can e.g. have different configurations for testing than for real production, 
by simply having different scripts configuring for the different scenarios.

In this respect the configuration code plays the role that e.g. a Spring xml configuration
file plays when true IOC is used.

@author Darren Cruse
*/

import com.mop.factorynew.AddService
import com.mop.factorynew.AddServiceImpl

// Installs support for "new()" and "creator()" on all classes/interfaces 
// (ideally would like to hide this behind an annotation or something - maybe someday...)
NewMethods.init()

// The default implementation of <Class>.new(args) is equivalent to "new <Class>(args)"
AddService adder1 = AddServiceImpl.new()

// This shows adder1 really is AddServiceImpl as expected:
println adder1.class.name
println adder1.add(3,5)
println adder1.addMap(a: 5, b: 3)

// This is how you could set the default implementation for code
// ceating an AddService using just the interface:
AddService.creator { 
	println "in creator closure"; 
	new AddServiceImpl() 
}

// Here we are getting the default new behavior which is
// to simply return an instance of the AddServiceClient class:
def adder2 = AddService.new()
// above is the same as:  AddService adder2 = AddService.new()

// This shows adder2 is an AddServiceImpl due to the registered creator closure:
println adder2.class.name
println adder2.add(5,3)
println adder2.addMap(a: 5, b: 3)

// Somewhat mind boggling is that even poor coding such as above for adder1
// can be overridden so long as they've used the "new method" approach.  In
// this case we're using a mock:
AddServiceImpl.creator { 
	println "in creator mock"
	[
		add: { a, b -> a + b }, 
		addMap: { Map args -> args.a + args.b }
	] as AddService 
}

// Note the direct reference to AddServiceImpl is returning the mock!
AddService adder3 = AddServiceImpl.new()

// This shows adder3 is a mock - and that it works!
println adder3.class.name
println adder3.add(5,3)
println adder3.addMap(a: 5, b: 3)

// Last example illustrates that it might not always be convenient or desirable
// to have an interface for everything you want to break a dependency on - in
// that case all you really need is a unique name by which to reference the 
// factory:
"addmeister".creator {  
		println "in addmeister creator closure"; 
		new AddServiceImpl() 
}

// Now we invoke this factory much like the others but using the name in place 
// of the class/interface:
def adder4 = "addmeister".new()

// This shows adder4 works like the others:
println adder4.class.name
println adder4.add(5,3)
println adder4.addMap(a: 5, b: 3)

// We've been using closures as "creators" above, but you can also use
// objects directly if you wish.  

// By making an object a singleton all calls to new() will get exactly the same instance:
def theAddService = new AddServiceImpl();  // this is the singleton AddService.

// Note here instead of "creator" we use "singleton"
AddService.singleton theAddService

def adder5 = AddService.new()
def adder6 = AddService.new()

// Note these are the same object:
println adder5
println adder6
assert adder5 == adder6

// Alternatively you can specify an object as a "prototype"
// i.e. where all references to new() return a clone of the prototype object:
// Note: the object used must be cloneable otherwise an exception is thrown.
def aDate = new Date();  // this is a prototype Date object.

// Note here instead of "creator" we use "prototype"
Date.prototype aDate

def date1 = Date.new()
def date2 = Date.new()

// Note these are *not* the same object:
println date1
println date2
assert !date1.is(date2)