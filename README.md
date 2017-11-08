[![Build Status](https://travis-ci.org/servicecatalog/oscm-interfaces.svg?branch=master)](https://travis-ci.org/servicecatalog/oscm-interfaces)
# oscm-interfaces
This repository contains interfaces for integrating your applications with 
[Open Service Catalog Manager](https://github.com/servicecatalog/oscm#open-service-catalog-manager).


**REST APIs**
 * Most needed OSCM Platform functionality is exposed with REST APIs. 
Find them [here](https://github.com/servicecatalog/oscm-rest-api).
	
**Web Service API**
 * oscm-extsvc - OSCM Platform Service API (inbound)
 * oscm-extsvc-provisioning - Provisioning Service API (outbound)
 * oscm-extsvc-notification - Notification Service API (outbound)
 * oscm-extsvc-operation - Operation Service API (outbound)
 
**IaaS Provisioning Proxy**  
 * oscm-app-extsvc - Java API for implementing cloud adapter
  
**OSCM internal Java API**
 * oscm-extsvc-internal - internal API based on oscm-extsvc

## How to use ##

For integrating with your project simply include them in your Maven pom. 

Example:
```
<repositories>
  <repository>
    <id>jitpack.io</id>
    <url>https://jitpack.io</url>
  </repository>
</repositories>

<dependency>
  <groupId>com.github.servicecatalog.oscm-interfaces</groupId>
  <artifactId>oscm-extsvc-provisioning</artifactId>
  <version>1.0</version>
</dependency>
```
All binaries and javadoc (TODO) as well as Web Service description (WSDL+XSD) can be downloaded [here](https://github.com/servicecatalog/oscm-interfaces/releases/tag/1.0). 

## Documentation
Detailed documentation describing the public OSCM Web services and application programming interfaces and how to integrate applications and external systems with OSCM can be found in the OSCM Developer Guide (*Link to developer guide*).




  
