package edu.uchicago.lorell;



// Will Darling, pay no attention to this.


public class HelloJNI{  // Save as HelloJNI.java
    static {
        System.loadLibrary("HelloJNI"); // Load native library hello.dll (Windows) or libhello.so (Unixes)
        //  at runtime
        // This library contains a native method called sayHello()
    }

    // Declare an instance native method sayHello() which receives no parameter and returns void
    public native void sayHello();

}
