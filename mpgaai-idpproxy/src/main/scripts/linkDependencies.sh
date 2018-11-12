#!/bin/bash
###
### links the mpgaai-idpproxy jars and its dependencies into the parent native idp libs 
###

DEST_LIB_HOME=( "/opt/shibboleth-idp2-proxy/war/idp/WEB-INF/lib/" "/opt/shibboleth-idp2-proxy-test/war/idp/WEB-INF/lib/" )
SRC_LIB_HOME=("/opt/mpgaai-idpproxy/lib" "/opt/mpgaai-idpproxy-test/lib" )

ii=0
for DEST_LIB in "${DEST_LIB_HOME[@]}"
do
	BACKUP=${DEST_LIB}/backup
	SRC_LIB=${SRC_LIB_HOME[ii]}
	echo -e  "\n*** handling idpproxy dependencies in" ${DEST_LIB}
	echo "  * backup old dependencies to" ${BACKUP}
	if [ ! -d ${BACKUP} ]; then
		mkdir ${BACKUP}
	fi
	mv `find "${DEST_LIB}"  -maxdepth 1 -lname "${SRC_LIB}/*.jar"` ${BACKUP}
	echo "  * linking new depencendencies from" ${SRC_LIB}
	ln -s ${SRC_LIB}/*.jar ${DEST_LIB}/
	echo "  * done"
	((ii++))
done

echo "ok"
exit 0
