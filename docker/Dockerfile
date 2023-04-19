FROM corda/community:4.9-zulu-openjdk8

COPY certificates/   /opt/corda/certificates
COPY config/   /etc/corda
COPY cordapps/  /opt/corda/cordapps
COPY drivers/   /opt/corda/drivers
COPY node-infos/ /opt/corda/additional-node-infos
COPY network-parameters /opt/corda/

CMD ["sh", "-c", "java -jar /opt/corda/bin/corda.jar run-migration-scripts --core-schemas --app-schemas -f /etc/corda/node.conf ; java -jar /opt/corda/bin/corda.jar -f /etc/corda/node.conf"]

