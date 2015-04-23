/**
 * Now we add a bit of syntax sugar to encapsulate the "configuration"
 * steps behind a friendlier interface. 
 * 
 */

package com.mop.factorynew

// Now we define interfaces separate from the implementation classes:
interface ContentCreator {
	def displayCreatedWorks();
}

interface ContentProduct {
	String productTypeLabel();
}

interface ContentCatalog {
	List<ContentProduct> getContentProducts()
	def addProduct(ContentProduct product)
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

class Catalog implements ContentCatalog {
	List<ContentProduct> products = [];
	
	List<ContentProduct> getContentProducts() { products }	
	def addProduct(ContentProduct product) { products << product }
}

// The assigned implementation classes for classes/interfaces:
def configuredImplementations = [:]

// We call the closure way of creating the implementation "creator"
Class.metaClass.static.creator = { closure ->
	System.out.println "Installing creator closure for ${delegate.name}"; 
	configuredImplementations[delegate.name] = closure
}

// And the class way simply "impl"
Class.metaClass.static.impl = { implClass ->
	System.out.println "Installing implementation class for ${delegate.name}"; 
	configuredImplementations[delegate.name] = implClass
}

Class.metaClass.static.new = { args -> 
	println "In new method for ${delegate.name}"
	System.out.flush()
	
	def instance
	
	def implSpec = configuredImplementations[delegate.name]
	if(implSpec && implSpec instanceof Closure) {	
			if(args) {
				// This is for a constructor/new method that takes args
				instance = implSpec(args) 
			}
			else {
				// This is for the no arg constructor/new method
				instance = implSpec() 
			}					
	}
	else {
		def implClass
		if(implSpec && implSpec instanceof Class) {
			implClass = implSpec
		}
		else {
			// Nope try and use the thing itself 
			// (they've probably used the impl class directly)
			println "Using the class itself to create ${delegate.name}"
			implClass = delegate;
		}
		
		if(args) {
			// This is for a constructor that takes args
			instance = implClass.newInstance(args) 
		}
		else {
			// This is for the no arg constructor
			instance = implClass.newInstance() 	
		}		

	}
	
	instance
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
// (where now we're using our nicer syntax!)
ContentCreator.impl Author
ContentProduct.impl Book

// Let's make a singleton for the catalog
def theCatalog = Catalog.new()
theCatalog.addProduct(ContentProduct.new(title: 'Carrie'))
theCatalog.addProduct(ContentProduct.new(title: 'The Shining'))
theCatalog.addProduct(ContentProduct.new(title: 'It'))
theCatalog.addProduct(ContentProduct.new(title: 'The Da Vinci Code'))

ContentCatalog.creator { 
	println "Returning the singleton Catalog"
	theCatalog 
}

def anotherReferenceToTheCatalog = ContentCatalog.new()
println "The catalog contains: " + anotherReferenceToTheCatalog.getContentProducts()

//def author = createWithRubyStyleNew()
//author.displayCreatedWorks()
//assert 3 == author.works.size()
//assert 'Stephen King' == author.name
//assert 'Carrie, The Shining, It' == author.works.title.join(', ')

