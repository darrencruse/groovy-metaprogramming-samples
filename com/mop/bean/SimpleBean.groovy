package com.mop.bean

class SimpleBean {
	String firstName
	String lastName	
	int age
	
	String toString() { "$firstName $lastName" }
	
	public static SimpleBean getTheBean() {
		println "Returning a bean"
		// def x = 1 / 0
		new SimpleBean(firstName: "Darren", lastName: "Cruse", age: 45)
	}
			
	public static List getSomeBeans() {
		println "Returning some beans"
		[new SimpleBean(firstName: "Darren", lastName: "Cruse", age: 45),
		 new SimpleBean(firstName: "Dawn", lastName: "Cruse", age: 48),
		 new SimpleBean(firstName: "Clint", lastName: "Cruse", age: 51)]		
	}
		
	static main(args) {
		System.out.println theBean 
	}
}
