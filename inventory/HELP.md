# Getting Started

## Hardware inventory and change history

The main application groups inventoried hardware by model (for example, processors and video controllers) and shows how many computers report each model. Open a model to inspect the computers where it is present and its change history. Open a computer from that list to review its current components and computer-specific history.

Use the name and component-category filters above the table, then choose Search. Reset clears both filters. On a narrow screen, expand Filters to show them.

The history records hardware appearing, disappearing or being replaced. A replacement of the same disk, memory or baseboard model is recorded only when a reliable serial number changes. A model-name change with the same stable identifier updates the catalog without a hardware event. Ordinary WMI property changes update the current details without adding history entries; old property-change entries are hidden. For equipment already present when history is first enabled, the journal says **First recorded** rather than guessing an installation date. Removals are recorded only after all WMI hardware queries for a computer complete successfully; failed scans preserve the previous inventory. A disappearance is an investigation clue, not proof of theft. History starts with the first successful scan after deployment; older changes are not available from the current-state database. Multiple identical devices on one computer and replacements without reliable serial numbers cannot be distinguished by the current model-level inventory. Without a stable identifier, a model rename may still look like a removal and installation.

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.4.0/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.4.0/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.4.0/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.4.0/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Liquibase Migration](https://docs.spring.io/spring-boot/3.4.0/how-to/data-initialization.html#howto.data-initialization.migration-tool.liquibase)
* [Java Mail Sender](https://docs.spring.io/spring-boot/3.4.0/reference/io/email.html)
* [OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Additional Links
These additional references should also help you:

* [Declarative REST calls with Spring Cloud OpenFeign sample](https://github.com/spring-cloud-samples/feign-eureka)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

