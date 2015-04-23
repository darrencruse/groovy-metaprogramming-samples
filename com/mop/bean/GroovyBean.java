package com.mop.bean;
import groovy.lang.GroovyObject;
import groovy.lang.MetaClass;
import org.codehaus.groovy.runtime.GStringImpl;
import org.codehaus.groovy.runtime.ScriptBytecodeAdapter;
import org.codehaus.groovy.runtime.callsite.CallSite;
import org.codehaus.groovy.runtime.typehandling.DefaultTypeTransformation;

public class GroovyBean
  implements GroovyObject
{
  private String name;
  private int age;

  public GroovyBean()
  {
    GroovyBean this;
    CallSite[] arrayOfCallSite = $getCallSiteArray();
    MetaClass tmp12_9 = $getStaticMetaClass(); this.metaClass = ((MetaClass)ScriptBytecodeAdapter.castToType(tmp12_9, $get$$class$groovy$lang$MetaClass())); tmp12_9;
  }

  public String toString()
  {
    CallSite[] arrayOfCallSite = $getCallSiteArray(); return (String)ScriptBytecodeAdapter.castToType(new GStringImpl(new Object[] { this.name, DefaultTypeTransformation.box(this.age) }, new String[] { "", " + ", "" }), $get$$class$java$lang$String());
  }

  public static void main(String[] args) {
    CallSite[] arrayOfCallSite = $getCallSiteArray(); arrayOfCallSite[0].call(arrayOfCallSite[1].callGetProperty($get$$class$java$lang$System()), arrayOfCallSite[2].callConstructor($get$$class$GroovyBean(), ScriptBytecodeAdapter.createMap(new Object[] { "name", "darren", "age", $const$0 }))); return;
  }

  static
  {
     tmp10_7 = new Long(0L);
    __timeStamp__239_neverHappen1274356317736 = (Long)tmp10_7;
    tmp10_7;
     tmp28_25 = new Long(1274356317736L);
    __timeStamp = (Long)tmp28_25;
    tmp28_25;
     tmp45_42 = new Integer(45);
    $const$0 = (Integer)tmp45_42;
    tmp45_42;
    return;
  }

  public String getName()
  {
    return this.name;
  }

  public void setName(String paramString)
  {
    this.name = paramString;
  }

  public int getAge()
  {
    return this.age;
  }

  public void setAge(int paramInt)
  {
    this.age = paramInt;
  }
}