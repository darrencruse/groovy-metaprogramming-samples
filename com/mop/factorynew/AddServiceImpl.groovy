package com.mop.factorynew

public class AddServiceImpl implements AddService {
	def add(a,b) { a + b }
	def addMap(Map args) { add(args.a, args.b) }	
}
