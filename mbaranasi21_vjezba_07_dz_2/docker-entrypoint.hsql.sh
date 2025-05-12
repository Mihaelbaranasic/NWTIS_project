#!/bin/bash
set -o errexit

if [ "$1" = 'hsqldb' ]; then
	java_vm_parameters="-Dfile.encoding=UTF-8"

# hsqldb server.properties file
cat <<EOF > server.properties
server.port=${HSQLDB_PORT:-9001}
server.silent=${HSQLDB_SILENT:-true}
server.trace=${HSQLDB_TRACE:-false}
server.remote_open=${HSQLDB_REMOTE:-true}
server.database.0=file:/opt/data/${HSQLDB_DATABASE_NAME}
server.dbname.0=${HSQLDB_DATABASE_NAME}
EOF

	echo "Starting HSQLDB: ${HSQLDB_DATABASE_NAME}"
	exec java ${java_vm_parameters} -cp /opt/hsqldb/hsqldb.jar org.hsqldb.server.Server
else
	exec "/bin/bash"
fi