# Latency benchmark appliction for Cloud Foundry

This is a set of applications that enables measurement of the latency overhead of running an application on Cloud Foundry. 
The benchmark comes with two sets of applications, one to measure the overhead of running HTTP based applications and one to 
handle low level TCP based communication. The applications can also be configured to use regular communication paths into
Cloud Foundry, as well as Container-to-Container networking of increased performance.

More documentation can be found at the following locations:
* [HTTP Based benchmark](http-test-apps)
* [TCP Based benchmark](tcp-test-apps)
