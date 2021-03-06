/**
 * A test script to see if we could do InterfaceName.new(), which would
 * logically be the way for code to be decoupled from implementations.
 *
 * Now we've made our new method "data driven" by using a map from the
 * interface name to the implementation class.
 */

package com.mop.factorynew

// Now we define interfaces separate from the implementation classes:
interface ContentCreator {
	def displayCreatedWorks();
}

interface ContentProduct {
	String productTypeLabel();
}

// Authors are "content creators"
class Author implements ContentCreator {
    String name
    List<ContentProduct> works

	def displayCreatedWorks() { println "$name, $works" }
}

// Books are a type of "content product"
class Book implements ContentProduct { 
    String title 

	String productTypeLabel() { "Hardcover Book" }
	
	@Override
	String toString() { "${title} (${productTypeLabel()})" }
}

// The default implementation classes for classes/interfaces:
def configuredImplementations = [:]

Class.metaClass.static.new = { args -> 
	println "In new method for ${delegate.name}"
	System.out.flush()
	
	// Is there an impl class configured for this?
	def implClass = configuredImplementations[delegate.name]
	if(!implClass) {
		// Nope try and use the thing itself 
		// (they've probably used the impl class directly)
		implClass = delegate;
	}
	implClass.newInstance(args) 	
}

// Now we use interfaces to decouple from specific implementation
// classes (e.g. so we could mock things for testing)
def createWithRubyStyleNew() {
    ContentCreator.new(name: 'Stephen King', works: [
        ContentProduct.new(title: 'Carrie'),
        ContentProduct.new(title: 'The Shining'),
        ContentProduct.new(title: 'It')
    ])
}

// Configure the default implementations for our interfaces:
configuredImplementations["com.mop.factorynew.ContentCreator"] = Author
configuredImplementations["com.mop.factorynew.ContentProduct"] = Book

def author = createWithRubyStyleNew()
author.displayCreatedWorks()
assert 3 == author.works.size()
assert 'Stephen King' == author.name
assert 'Carrie, The Shining, It' == author.works.title.join(', ')

