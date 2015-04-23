/**
 * A test script to see if I could add "Ruby style new" classes on Groovy classes. 
 */

package com.mop.factorynew

class Author {
    String name
    List books
}
class Book { 
    String title 
}

Class.metaClass.static.new = { args -> 
	println "In new method for ${delegate.name}"; 
	delegate.newInstance(args) 
}

def createWithRubyStyleNew() {
    Author.new(name: 'Stephen King', books: [
        Book.new(title: 'Carrie'),
        Book.new(title: 'The Shining'),
        Book.new(title: 'It')
    ])
}

def author = createWithRubyStyleNew()
assert 3 == author.books.size()
assert 'Stephen King' == author.name
assert 'Carrie, The Shining, It' == author.books.title.join(', ')