package com.mop.bean

class SimpleClass {

    String heading = ""
    int count = 0

	public int add(int a, int b) { a + b } 	

//	public closure = { "this is a closure!" }

    private void printItem(String item) {
        System.out.println("item " + count + ": " + item)
        count++
    }

    public void printItems(String[] items) {
        System.out.println(heading)
        for(String item: items) {
            printItem(item)
        }
    } 
}	

