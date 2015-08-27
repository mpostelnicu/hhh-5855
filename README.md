# HHH-5855
Hibernate Bug HHH-5855 Test Case

This project provides a test case that reproduces the bug HHH-5855 on Hibernate 4.3.11.Final (5-Aug-2015).

HHH-5855 The method "merge" from the EntityManager causes a duplicated "insert" of a child entity.
https://hibernate.atlassian.net/browse/HHH-5855

To run the test case just invoke:

```
mvn install
```


For more info see Vlad Mihalcea's blog post and github page:

http://vladmihalcea.com/2013/10/16/hibernate-facts-favoring-sets-vs-bags/

https://github.com/vladmihalcea/vladmihalcea.wordpress.com
