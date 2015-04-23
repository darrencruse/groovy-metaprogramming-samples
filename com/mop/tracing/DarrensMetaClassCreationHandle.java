/**
 * Class used as base for the creation of MetaClass implementations.
 * The Class defaults to MetaClassImpl, if the class loading fails to
 * find a special meta class. The name for such a meta class would be
 * the class name it is created for with the prefix
 * "groovy.runtime.metaclass." By replacing the handle in the registry
 * you can have any control over the creation of what MetaClass is used
 * for a class that you want to have. 
 * WARNING: experimental code, likely to change soon
 * @author Jochen Theodorou
 */
package com.mop.tracing;

import org.codehaus.groovy.runtime.GeneratedClosure;
import org.codehaus.groovy.runtime.metaclass.ClosureMetaClass;

import java.lang.reflect.Constructor;
import java.util.Iterator;

class DarrensMetaClassCreationHandle {
    private boolean disableCustomMetaClassLookup;

    public final MetaClass create(Class theClass, MetaClassRegistry registry) {
       if (disableCustomMetaClassLookup)
           return createNormalMetaClass(theClass, registry);

        return createWithCustomLookup(theClass, registry);
    }

    private MetaClass createWithCustomLookup(Class theClass, MetaClassRegistry registry) {
        try {
            final Class customMetaClass = Class.forName("groovy.runtime.metaclass." + theClass.getName() + "MetaClass");
            if (DelegatingMetaClass.class.isAssignableFrom(customMetaClass)) {
                final Constructor customMetaClassConstructor = customMetaClass.getConstructor(MetaClass.class);
                MetaClass normalMetaClass = createNormalMetaClass(theClass, registry);
                return (MetaClass)customMetaClassConstructor.newInstance(normalMetaClass);
            }
            else {
                final Constructor customMetaClassConstructor = customMetaClass.getConstructor(MetaClassRegistry.class, Class.class);
                return (MetaClass)customMetaClassConstructor.newInstance(registry, theClass);
            }
        }
        catch (final ClassNotFoundException e) {
            return createNormalMetaClass(theClass, registry);
        } catch (final Exception e) {
            throw new GroovyRuntimeException("Could not instantiate custom Metaclass for class: " + theClass.getName() + ". Reason: " + e, e);
        }
    }

    protected MetaClass createNormalMetaClass(Class theClass,MetaClassRegistry registry) {
        if (GeneratedClosure.class.isAssignableFrom(theClass)) {
            return new ClosureMetaClass(registry,theClass);
        } else {
            return new MetaClassImpl(registry, theClass);
        }
    }

    public boolean isDisableCustomMetaClassLookup() {
        return disableCustomMetaClassLookup;
    }

    /**
     * Set flag saying to disable lookup of custom meta classes
     * It's enough to call this method only once in your application for handle which was set in to registry
     * as every new handle will inherit this property
     * @param disableCustomMetaClassLookup flag saying to disable lookup of custom meta classes
     */
    public void setDisableCustomMetaClassLookup(boolean disableCustomMetaClassLookup) {
        this.disableCustomMetaClassLookup = disableCustomMetaClassLookup;
    }
}
