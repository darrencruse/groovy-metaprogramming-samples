/**
 * A test script to see if we could do InterfaceName.new(), which would
 * logically be the way for code to be decoupled from implementations.
 *
 * In this we make our new method watch for the interface names and 
 * create instances of the appropriate implementation classes.
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
	
	String toString() { "${title} (${productTypeLabel()})" }
}

Class.metaClass.static.new = { args -> 
	println "In new method for ${delegate.name}";
	System.out.flush();	
	switch (delegate.name) {
		case "com.mop.factorynew.ContentCreator":
			println "Creating an Author as the default ContentCreator implementation";	
			new Author(args)
			break
		case "com.mop.factorynew.ContentProduct":
				println "Creating a Book as the default ContentProduct implementation";	
				new Book(args)
				break	
		default:
			// Default to creating an instance using newInstance:
			delegate.newInstance(args) 	
	}	
	
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

def author = createWithRubyStyleNew()
author.displayCreatedWorks()
assert 3 == author.works.size()
assert 'Stephen King' == author.name
assert 'Carrie, The Shining, It' == author.works.title.join(', ')

