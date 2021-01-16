<h3 align="center">Bulk Transfer Application</h3>

## Usage

The application can be used to import the KMOT customer data extract as CSV from AD/LDS and upload it into the FR DS.

## Local Development

### Prerequisite

- The FR DS should be up and running on LDAPS port.
- The SSL handshake should be configured. There are two option(s):
  - The FR DS certificate can be used
  - The FR DS keystore can be used

### SSL Handshake Configuration

#### Option 1: How to use FR DS certificate

```sh
$ keytool -export -alias server-cert -file /tmp/server-cert.crt -keystore /home/opendj/opendj/config/keystore  
-storepass `cat /home/opendj/opendj/config/keystore.pin`
```
Execute this command from the docker container running the FR DS to export the certificate

```sh
$ docker cp CONTAINER_ID:/tmp/server-cert.crt .
```
Copy the certificate from the docker container into the host machine

```sh
$ keytool -importcert -alias server-cert -file server-cert.crt -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt
```
Execute this command from the host machine to import the certificate into the JVM

#### Option 2: How to use FR DS keystore

```sh
$ docker cp CONTAINER_ID:/home/opendj/opendj/config/keystore .
```
Copy the keystore to the host machine

```sh
$ docker cp CONTAINER_ID:/home/opendj/opendj/config/keystore.pin .
```
Copy the keystore password file to the host machine

### How to run the application

```sh
$ export $(cat config/local.env | xargs)
```
Modify the file config/local.env as per the environment and export it
- OPENDJ_CERTS_DIR: This attribute will have the keystore folder path
- OPENDJ_CERT_FILE: This attribute will have the keystore file name (FR DS or host machine JVM)
- OPENDJ_CERT_STOREPASS: This attribute will have the keystore password (FR DS or host machine JVM)

```sh
$ mvn -s config/settings.xml -U clean install
$ java -jar target/bulk-transfer-app-1.0.jar path=FOLDER_PATH
```
- The CSV file name should be customers.csv and it should be available in the FOLDER_PATH
- The SWA intersection customers CSV file will be creted in the FOLDER_PATH
- The SWW intersection customers CSV file will be creted in the FOLDER_PATH
- The SWW exception customers CSV file will be creted in the FOLDER_PATH

## Note

The application does not sanitize the user data it reads from the CSV.
