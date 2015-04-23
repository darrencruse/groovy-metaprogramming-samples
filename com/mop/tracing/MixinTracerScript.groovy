package com.mop.tracing

public class MPTracer implements GroovyInterceptable {
        
    Object invokeMethod(String name, Object args) {
        
        System.out.println("            =>  ${name}(${args.join(', ')})");
// Approach a:				
//        def result = this.metaClass.invokeMethod(this, name, args)

// Approach b:
        def metaMethod = this.metaClass.getMetaMethod(name, args)
        def result = null
        if(metaMethod)
        {
            System.out.println "Found $name()"           
            result = metaMethod.invoke(this, args)
        }
        
        System.out.println("            <=  ${name}(${args.join(', ')} returned $result");
        
        result
    }    
}

public class MPTraced  {
    
    public int add(int a, int b) { a + b }     
   
}
    
MPTraced.mixin MPTracer        
MPTraced mpt = new MPTraced();
println mpt.add(2,4);

