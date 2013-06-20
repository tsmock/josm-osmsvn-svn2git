package org.j7zip.Common;

public class ObjectVector<E> extends java.util.Vector<E>
{
    public ObjectVector() {
        super();
    }
    
    public void Reserve(int s) {
        ensureCapacity(s);
    }
    
    public E Back() {
        return get(elementCount-1);
    }
    
    public E Front() {
        return get(0);
    }
    
    public void DeleteBack() {
        remove(elementCount-1);
    }
}
