# javadepextractor
It extracts all dependencies of a Java system into a txt file.

Usage (get the last release in [dist] directory:
> javadepextractor.jar [folder-dir]

For example, if the *javadepextractor.jar* is in the root source:
> java -jar javadepextractor.jar .

It creates the *dependencies.txt* file in which **each** line is as follows:
> [source-class-full-qualified-name] , [dependency-type] , [target-class-full-qualified-name]

For example:
> com.terra.ClassA , access , java.lang.Math

Important: javadepextractor does not support type inference yet. For example, the dependencies inside a lambda expression that does not specify the type of its elements, will not be extracted. 

PS: The dependency type can be: access, declare, create, extend, implement, useannotation, and throw.
