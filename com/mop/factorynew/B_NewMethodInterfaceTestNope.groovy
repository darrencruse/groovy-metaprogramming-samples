/**
 * A test script to see if we could do InterfaceName.new(), which would
 * logically be the way for code to be decoupled from implementations.
 * 
 * This fails since there's no constructor for ContentCreator - it's an
 * interface!
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
}

Class.metaClass.static.new = { args -> 
	println "In new method for ${delegate.name}";
	System.out.flush();
	delegate.newInstance(args) 
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
assert 3 == author.works.size()
assert 'Stephen King' == author.name
assert 'Carrie, The Shining, It' == author.works.title.join(', ')
