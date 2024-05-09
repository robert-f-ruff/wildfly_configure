# WildFly Configure
WildFly Configure is a utility for a WildFly server running in a Docker container. The utility performs the following functions in this order:
1. Generates a WildFly YAML configuration file from a template
2. Controls the Docker container startup sequence

The utility runs inside the Docker container before the WildFly Server starts.

## WildFly YAML Configuration File Generation
This capability ensures that sensitive information, such as IP addresses, usernames, and passwords, is not stored in the Docker image. The image contains a clean standalone.xml configuration file that the WildFly server configures during bootup using a YAML configuration file. Sensitive information is provided through Docker's secrets functionality. The utility, as an intermediary, takes the information provided in Docker secrets and places it in a format (a YAML configuration file) that the WildFly server can easily consume.

The utility reads a WildFly YAML template file and the Docker container's mounted secrets then writes the YAML configuration file. In the template file, locations where a Docker secret belongs are identified by a dollar sign followed by a set of opening and closing braces. The name of the secret is enclosed within the braces. For example, `${db_host}` identifies the db_host secret. The utility replaces the dollar brace identifier with the value of the secret itself. The `src/test/resources/wildfly_config.yml.tmpl` is an example template file.

## Pause Container Startup
When the WildFly Server is configured with a data source, it attempts to connect to the data source upon bootup. If the configured data source is part of the same Docker Compose application group as the WildFly Server container, there is a high probability that the WildFly server will attempt to connect to the data source before the data source container is ready to accept connections. The utility will prevent the WildFly server from starting until it has verified that the data source container is accepting connections.

The utility uses the data source connection information it obtained from the Docker secrets to test the data source connection every two seconds until a connection is successfully made. At this point, the utility closes the connection and stops running, thereby allowing the WildFly server to start and successfully connect with the data source container. Refer to the table below for the list of secrets that the utility looks for connection information in.

<table>
  <caption>Data Source Connection Secrets</caption>
  <thead>
    <tr>
      <th scope="col">Connection Attribute</th>
      <th scope="col">Secret Name</th>
      <th scope="col">Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th scope="row">Host Address</td>
      <td>db_host</th>
      <td>Network address of the data source</td>
    </tr>
    <tr>
      <th scope="row">Host Port Number</td>
      <td>db_host_port</th>
      <td>Port number the data source listens on for connections</td> 
    </tr>
    <tr>
      <th scope="row">Username</td>
      <td>db_user_name</th>
      <td>Name of an unprivileged user account to use in connecting to the data source</td>
    </tr>
    <tr>
      <th scope="row">User Password</td>
      <td>db_user_password</th>
      <td>Password for the unprivileged user account</td>
    </tr>
  </tbody>
</table>

# Development Setup

## Prerequisites
Install the following tools:
- [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/)
- [Apache Maven v3.9.6](https://maven.apache.org/download.cgi)

## Build and Distribute the Utility
The utility is packaged as an executable JAR file and distributed via the local Maven repository:

```Shell
mvn install
```

# Execution
The utility is launched from the command line:

```Shell
java -cp <path_to_utility_jar>/wildfly-configuration-1.0.jar:<path_to_jdbc_jar>/mysql-connector-j-8.3.0.jar io.github.robert_f_ruff.wildfly_configuration.Main \
    <template_file> <config_file> <secrets_path>
```

where:
- `<path_to_utility_jar>` is the absolute path where the utility's JAR file
- `<path_to_jdbc_jar` is the absolute path to the data source's JDBC JAR file
- `<template_file>` is the absolute path and filename of the YAML configuration template file
- `<config_file>` is the absolute path and filename of the YAML configuration file to generate
- `<secrets_path>` is the absolute path where the Docker secrets are mounted