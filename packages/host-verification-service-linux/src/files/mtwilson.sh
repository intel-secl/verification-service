#!/bin/bash

### BEGIN INIT INFO
# Provides:          mtwilson
# Required-Start:    $local_fs $network $remote_fs
# Required-Stop:     $local_fs $network $remote_fs
# Default-Start:     2 3 4 5
# Default-Stop:      0 1 6
# Short-Description: Start mtwilson webserver
# Description:       Enable mtwilson services
### END INIT INFO

# WARNING:
# *** do NOT use TABS for indentation, use SPACES
# *** TABS will cause errors in some linux distributions
DESC="MTWILSON"
NAME=mtwilson

# the home directory must be defined before we load any environment or
# configuration files; it is explicitly passed through the sudo command
export MTWILSON_HOME=${MTWILSON_HOME:-/opt/mtwilson}

# the env directory is not configurable; it is defined as MTWILSON_HOME/env and the
# administrator may use a symlink if necessary to place it anywhere else
export MTWILSON_ENV=$MTWILSON_HOME/env

mtw_load_env() {
  local env_files="$@"
  local env_file_exports
  for env_file in $env_files; do
    if [ -n "$env_file" ] && [ -f "$env_file" ]; then
      . $env_file
      env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
      if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
    fi
  done
}

# load environment variables; these override any existing environment variables.
# the idea is that if someone wants to override these, they must have write
# access to the environment files that we load here. 
if [ -d $MTWILSON_ENV ]; then
   mtw_load_env $(ls -1 $MTWILSON_ENV/*)
fi

if [ -z "$MTWILSON_USERNAME" ]; then
  mtw_load_env $MTWILSON_HOME/env/mtwilson-username
fi


###################################################################################################

# if non-root execution is specified, and we are currently root, start over; the MTW_SUDO variable limits this to one attempt
# we make an exception for the uninstall command, which may require root access to delete users and certain directories
if [ -n "$MTWILSON_USERNAME" ] && [ "$MTWILSON_USERNAME" != "root" ] && [ $(whoami) == "root" ] && [ -z "$MTWILSON_SUDO" ] \
  && [ "$1" != "uninstall" ] && [[ "$1" != "replace-"* ]]; then
  export MTWILSON_SUDO=true
  sudo -u $MTWILSON_USERNAME -H -E "$MTWILSON_BIN/mtwilson" $*
  exit $?
fi

###################################################################################################

# default directory layout follows the 'home' style
export MTWILSON_CONFIGURATION=${MTWILSON_CONFIGURATION:-${MTWILSON_CONF:-$MTWILSON_HOME/configuration}}
export MTWILSON_JAVA=${MTWILSON_JAVA:-$MTWILSON_HOME/java}
export MTWILSON_BIN=${MTWILSON_BIN:-$MTWILSON_HOME/bin}
export MTWILSON_REPOSITORY=${MTWILSON_REPOSITORY:-$MTWILSON_HOME/repository}
export MTWILSON_LOGS=${MTWILSON_LOGS:-$MTWILSON_HOME/logs}

MTWILSON_INSTALL_LOG_FILE=${MTWILSON_INSTALL_LOG_FILE:-/tmp/mtwilson-install.log}
export INSTALL_LOG_FILE="$MTWILSON_INSTALL_LOG_FILE"

####################################################################################################

# load linux utility
if [ -f "$MTWILSON_HOME/bin/functions.sh" ]; then
  . $MTWILSON_HOME/bin/functions.sh
fi

####################################################################################################
# stored master password
if [ -z "$MTWILSON_PASSWORD" ] && [ -f $MTWILSON_CONFIGURATION/.mtwilson_password ]; then
  export MTWILSON_PASSWORD=$(cat $MTWILSON_CONFIGURATION/.mtwilson_password)
fi

# FUNCTION LIBRARY and VERSION INFORMATION
if [ -f /opt/mtwilson/configuration/version ]; then  . /opt/mtwilson/configuration/version; else  echo_warning "Missing file: /opt/mtwilson/configuration/version"; fi
shell_include_files ${MTWILSON_ENV}/*

if [[ "$@" != *"ExportConfig"* ]]; then   # NEED TO DEBUG FURTHER. load_conf runs ExportConfig and if that same command is passed in from 'mtwilson setup', it won't work
  load_conf 2>&1 >/dev/null
  if [ $? -ne 0 ]; then
    if [ $? -eq 2 ]; then echo_failure -e "Incorrect encryption password. Please verify \"MTWILSON_PASSWORD\" variable is set correctly."; fi
    exit -1
  fi
fi
load_defaults 2>&1 >/dev/null

# add mtwilson included libraries to LD_LIBRARY_PATH
libFolders=$(find /opt/mtwilson/share -name lib -not -path "/opt/mtwilson/share/apache-tomcat*" 2>/dev/null)
for libFolder in $libFolders; do
  libFoldersString+="$libFolder:"
done
export LD_LIBRARY_PATH=$(echo $libFoldersString$LD_LIBRARY_PATH | sed 's/:$//g')

# all other variables with defaults
MTWILSON_APPLICATION_LOG_FILE=${MTWILSON_APPLICATION_LOG_FILE:-$MTWILSON_LOGS/mtwilson.log}
chown "$MTWILSON_USERNAME":"$MTWILSON_USERNAME" "$MTWILSON_APPLICATION_LOG_FILE"
chmod 600 "$MTWILSON_APPLICATION_LOG_FILE"

MTWILSON_HTTP_LOG_FILE=${MTWILSON_HTTP_LOG_FILE:-$MTWILSON_LOGS/http.log}
JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.7}
JAVA_OPTS=${JAVA_OPTS:-"-Dlogback.configurationFile=$MTWILSON_CONFIGURATION/logback.xml -Dlog4j.configuration=file:$MTWILSON_CONFIGURATION/log4j.properties -Djdk.tls.ephemeralDHKeySize=2048"}
JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true"

MTWILSON_SETUP_FIRST_TASKS=${MTWILSON_SETUP_FIRST_TASKS:-"filesystem update-extensions-cache-file"}
MTWILSON_SETUP_MANAGER_TASKS="update-ssl-port jetty-ports create-tls-certificate password-vault shiro-ssl-port create-endorsement-ca create-privacy-ca";
MTWILSON_PRESETUP_TASKS="create-data-encryption-key"
MTWILSON_SETUP_TASKS=${MTWILSON_SETUP_TASKS:-"create-certificate-authority-key create-flavor-signing-certificate create-default-flavorgroups sign-existing-unsigned-flavors create-saml-certificate"}

# RHEL 7.6 needs PID file for systemd startup service
# The location is identified as below in mtwilson.sh
MTWILSON_PID_FILE=$MTWILSON_HOME/mtwilson.pid


# RHEL 7.6 needs PID file for systemd startup service
# The location is identified as below in mtwilson.sh
MTWILSON_PID_FILE=$MTWILSON_HOME/mtwilson.pid

####################################################################################################
# java command
if [ -z "$JAVA_CMD" ]; then
  if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD=$JAVA_HOME/bin/java
  else
    JAVA_CMD=`which java`
  fi
fi

JARS=$(ls -1 $MTWILSON_JAVA/*.jar)
CLASSPATH=$(echo $JARS | tr ' ' ':')

# the classpath is long and if we use the java -cp option we will not be
# able to see the full command line in ps because the output is normally
# truncated at 4096 characters. so we export the classpath to the environment
export CLASSPATH
####################################################################################################
if no_java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION}; then echo "Cannot find Java ${JAVA_REQUIRED_VERSION:-$DEFAULT_JAVA_REQUIRED_VERSION} or later"; exit 1; fi

print_help() {
        echo -e "Usage: mtwilson help|start|stop|restart|status|uninstall|uninstall --purge|version|fingerprint|java-detect|erase-data|erase-users|zeroize"
        echo -e "Usage: mtwilson setup [--force|--noexec] [task1 task2 ...]" 
        echo -e "Usage: mtwilson login-password [username] [password] --permissions [domain1:action1]"
        echo -e "Usage: mtwilson export-config [outfile|--in=infile|--out=outfile|--stdout] [--env-password=PASSWORD_VAR]"
        echo -e "Usage: mtwilson config [key] [--delete|newValue]"
        echo -e "Usage: mtwilson replace-root-key-pair [--private-key=private-key-file] [--cert-chain=cert-chain-file]"
        echo -e "Usage: mtwilson replace-saml-key-pair [--private-key=private-key-file] [--cert-chain=cert-chain-file]"
        echo -e "Usage: mtwilson replace-tag-key-pair  [--private-key=private-key-file] [--cert-chain=cert-chain-file]"
        echo -e "Usage: mtwilson replace-pca-key-pair  [--private-key=private-key-file] [--cert-chain=cert-chain-file]"
        echo -e "Usage: mtwilson replace-eca-key-pair  [--private-key=private-key-file] [--cert-chain=cert-chain-file]"
        echo "Available setup tasks:"
        echo $MTWILSON_SETUP_MANAGER_TASKS | tr ' ' '\n'
		echo $MTWILSON_SETUP_TASKS | tr ' ' '\n'
		echo $MTWILSON_PRESETUP_TASKS | tr ' ' '\n'
		echo $MTWILSON_SETUP_FIRST_TASKS | tr ' ' '\n'
}

mtwilson_saml_cert_report() {
  local keystore=`$MTWILSON_BIN/mtwilson config saml.keystore.file`   #`read_property_from_file saml.keystore.file ${conf_dir}/attestation-service.properties`
  local keystorePassword=`$MTWILSON_BIN/mtwilson config saml.keystore.password`   #`read_property_from_file saml.keystore.password ${conf_dir}/attestation-service.properties`
  local keyalias=`$MTWILSON_BIN/mtwilson config saml.key.alias`   #`read_property_from_file saml.key.alias ${conf_dir}/attestation-service.properties`
  java_keystore_cert_report "${MTWILSON_CONFIGURATION}/$keystore" "$keystorePassword" "$keyalias"
}

mtwilson_tls_fingerprint() {
  local sha384=`read_property_from_file tls.cert.sha384 "${MTWILSON_CONFIGURATION}/https.properties"`
  echo "SHA384: $sha384"
}

mtwilson_run() {
  local args="$*"
  $JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $args
  return $?
}

mtwilson_complete_setup() {
  # run all setup tasks, don't use the force option to avoid clobbering existing
  # useful configuration files


  echo "Running database setup scripts..."
  $MTWILSON_BIN/mtwilson setup InitDatabasePostgresql

  mtwilson_generate_master_password
  mtwilson_encrypt_config
  mtwilson_setup_keystore

  # Setup config 
  MTWILSON_PORT_HTTP=${MTWILSON_PORT_HTTP:-${JETTY_PORT:-8442}}
  MTWILSON_PORT_HTTPS=${MTWILSON_PORT_HTTPS:-${JETTY_SECURE_PORT:-8443}}

  export TAG_PROVISION_EXTERNAL=${MTWILSON_TAG_CERT_IMPORT_AUTO:-false}
  $MTWILSON_BIN/mtwilson config "tag.provision.external" "$TAG_PROVISION_EXTERNAL" >/dev/null
  export TAG_PROVISION_XML_ENCRYPTION_REQUIRED=${TAG_PROVISION_XML_ENCRYPTION_REQUIRED:-false}
  $MTWILSON_BIN/mtwilson config "tag.provision.xml.encryption.required" "$TAG_PROVISION_XML_ENCRYPTION_REQUIRED" >/dev/null
  export TAG_PROVISION_XML_PASSWORD=${TAG_PROVISION_XML_PASSWORD:-TagProvisionPassword}
  $MTWILSON_BIN/mtwilson config "tag.provision.xml.encryption.password" "$TAG_PROVISION_XML_PASSWORD" >/dev/null

  $MTWILSON_BIN/mtwilson config jetty.port $MTWILSON_PORT_HTTP >/dev/null
  $MTWILSON_BIN/mtwilson config jetty.secure.port $MTWILSON_PORT_HTTPS >/dev/null

  $MTWILSON_BIN/mtwilson config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,jersey-media-multipart}" >/dev/null
  $MTWILSON_BIN/mtwilson config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null

  mtwilson_run setup $MTWILSON_SETUP_FIRST_TASKS
  mtwilson_run setup-manager $MTWILSON_SETUP_MANAGER_TASKS
  mtwilson_run setup $MTWILSON_PRESETUP_TASKS
  mtwilson_run setup $MTWILSON_SETUP_TASKS


  postsetup_config

  #mtwilson tag-init-database
  $MTWILSON_BIN/mtwilson tag-create-ca-key "CN=asset-tag-service"
  $MTWILSON_BIN/mtwilson tag-export-file cacerts | grep -v ":" >> ${MTWILSON_CONFIGURATION}/tag-cacerts.pem

}

postsetup_config() {
  # protect privacyca files
  PRIVACYCA_FILES="${MTWILSON_CONFIGURATION}/EndorsementCA.p12 ${MTWILSON_CONFIGURATION}/PrivacyCA.p12"
  chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $PRIVACYCA_FILES
  chmod 600 $PRIVACYCA_FILES

  cat ${MTWILSON_CONFIGURATION}/cacerts.pem >> ${MTWILSON_CONFIGURATION}/MtWilsonRootCA.crt.pem
  chown $MTWILSON_USERNAME:$MTWILSON_USERNAME ${MTWILSON_CONFIGURATION}/MtWilsonRootCA.crt.pem
}

mtwilson_setup() {
  local args="$*"

  $JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main setup $args
  return $?
}

# Helper method to setup SAML keystores
mtwilson_setup_keystore() {

  # Setup SAML params
  $MTWILSON_BIN/mtwilson config "saml.key.alias" "$SAML_KEY_ALIAS" >/dev/null

  # create saml key
  echo "Creating SAML key..."
  if [ ! -f $SAML_KEYSTORE_FILE ]; then
    SAML_KEYSTORE_FILE=${MTWILSON_CONFIGURATION}/${SAML_KEYSTORE_FILE}
  fi

  $MTWILSON_BIN/mtwilson config "saml.keystore.password" "changeit" >/dev/null
  $MTWILSON_BIN/mtwilson config "saml.key.password" "changeit" >/dev/null

  $MTWILSON_BIN/mtwilson config mtwilson.extensions.fileIncludeFilter.contains "${MTWILSON_EXTENSIONS_FILEINCLUDEFILTER_CONTAINS:-mtwilson,jersey-media-multipart}" >/dev/null
  $MTWILSON_BIN/mtwilson config mtwilson.extensions.packageIncludeFilter.startsWith "${MTWILSON_EXTENSIONS_PACKAGEINCLUDEFILTER_STARTSWITH:-com.intel,org.glassfish.jersey.media.multipart}" >/dev/null

  saml_issuer="https://${MTWILSON_SERVER:-127.0.0.1}:${MTWILSON_PORT_HTTPS:-8443}"
  $MTWILSON_BIN/mtwilson config "saml.issuer" "${saml_issuer}" >/dev/null
}

mtwilson_generate_master_password() {
  # the master password is required
  if [ -z "$MTWILSON_PASSWORD" ] && [ ! -f $MTWILSON_CONFIGURATION/.mtwilson_password ]; then
    touch $MTWILSON_CONFIGURATION/.mtwilson_password
    chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $MTWILSON_CONFIGURATION/.mtwilson_password
    $MTWILSON_BIN/mtwilson generate-password > $MTWILSON_CONFIGURATION/.mtwilson_password

    export MTWILSON_PASSWORD=$(cat $MTWILSON_CONFIGURATION/.mtwilson_password)
  fi

}

# Helper method to encrypt the mtwilson configuration
mtwilson_encrypt_config() {
  $MTWILSON_BIN/mtwilson import-config --in="${MTWILSON_CONFIGURATION}/mtwilson.properties" --out="${MTWILSON_CONFIGURATION}/mtwilson.properties" 2>/dev/null
}

mtwilson_start() {
  # the subshell allows the java process to have a reasonable current working
  # directory without affecting the user's working directory. 
  # the last background process pid $! must be stored from the subshell.
  (
    cd $MTWILSON_HOME
    "$JAVA_CMD" $JAVA_OPTS com.intel.mtwilson.launcher.console.Main jetty-start >>$MTWILSON_APPLICATION_LOG_FILE 2>&1 &
    echo $! > $MTWILSON_PID_FILE
  )
  echo_success "Started Host Verification Service"
}

mtwilson_start_check() {
  mtwilson_start 2>&1 >/dev/null
  sleep 5
  for (( i = 1; i <= 12; i++ )); do
    sleep 1
    if mtwilson_is_running; then
      break;
    elif (( $i % 3 == 0 )); then
      mtwilson_start 2>&1 >/dev/null
    fi
  done
  if mtwilson_is_running; then
    echo_success "Started Host Verification Service"
  else
    echo_failure "Failed to start Host Verification Service"
  fi
}

# returns 0 if MTWILSON is running, 1 if not running
# side effects: sets MTWILSON_PID if MTWILSON is running, or to empty otherwise
mtwilson_is_running() {
  MTWILSON_PID=
  if [ -f $MTWILSON_PID_FILE ]; then
    MTWILSON_PID=$(cat $MTWILSON_PID_FILE)
    local is_running=`ps -A -o pid | grep "^\s*${MTWILSON_PID}$"`
    if [ -z "$is_running" ]; then
      # stale PID file
      MTWILSON_PID=
    fi
  fi
  if [ -z "$MTWILSON_PID" ]; then
    # check the process list just in case the pid file is stale
    MTWILSON_PID=$(ps -A ww | grep -v grep | grep java | grep "com.intel.mtwilson.launcher.console.Main jetty-start" | grep "$MTWILSON_CONFIGURATION" | awk '{ print $1 }')
  fi
  if [ -z "$MTWILSON_PID" ]; then
    # MTWILSON is not running
    return 1
  fi
  # MTWILSON is running and MTWILSON_PID is set
  return 0
}

mtwilson_stop() {
  if mtwilson_is_running; then
    kill -9 $MTWILSON_PID
    if [ $? ]; then
      echo "Stopped Host Verification Service"
      # truncate pid file instead of erasing,
      # because we may not have permission to create it
      # if we're running as a non-root user
      echo > $MTWILSON_PID_FILE
    else
      echo "Failed to stop Host Verification Service"
    fi
  fi
}

# removes MTWILSON home directory (including configuration and data if they are there).
# if you need to keep those, back them up before calling uninstall,
# or if the configuration and data are outside the home directory
# they will not be removed, so you could configure MTWILSON_CONFIGURATION=/etc/kms
# and MTWILSON_REPOSITORY=/var/opt/kms and then they would not be deleted by this.
mtwilson_uninstall() {
    remove_startup_script mtwilson
	rm -f /usr/local/bin/mtwilson
    if [ -z "$MTWILSON_HOME" ]; then
      echo_failure "Cannot uninstall because MTWILSON_HOME is not set"
      return 1
    fi
    if [ "$1" == "--purge" ]; then
      rm -rf $MTWILSON_HOME $MTWILSON_CONFIGURATION $MTWILSON_DATA $MTWILSON_LOGS
    else
      rm -rf $MTWILSON_HOME/bin $MTWILSON_HOME/java $MTWILSON_HOME/features
    fi
    groupdel $MTWILSON_USERNAME > /dev/null 2>&1
    userdel $MTWILSON_USERNAME > /dev/null 2>&1
}

RETVAL=0

# See how we were called.
case "$1" in
  help)
    print_help
    ;;
  start)
    mtwilson_start_check
    ;;
  stop)
    mtwilson_stop
    ;;
  restart)
    mtwilson_stop
    mtwilson_start_check
    ;;
  status)
    if mtwilson_is_running; then
      echo "Host Verification Service is running"
      exit 0
    else
      echo "Host Verification Service is not running"
      exit 1
    fi
    ;;
  setup)
    shift
    if [ -n "$1" ]; then
      mtwilson_setup $*
    else
      mtwilson_complete_setup
    fi
    ;;
  erase-data)
        erase_data
		$MTWILSON_BIN/mtwilson setup create-default-flavorgroups >/dev/null 2>&1
        if [ $? -ne 0 ]; then exit 1; fi
        ;;
  erase-users)
        $MTWILSON_BIN/mtwilson erase-user-accounts $@
        ;;
  zeroize)
        echo "Shredding Host Verification Service configuration"
        cd /tmp && find "$MTWILSON_CONFIGURATION/" -type f -exec shred -uzn 3 {} \;
        ;;
  java-detect)
        java_detect $2
        java_env_report
        ;;
  fingerprint)
        # show server ssh fingerprint, ssl fingerprint, and saml cert fingerprint
        echo "TLS Certificate Fingerprint:"
		mtwilson_tls_fingerprint
        echo -e "\nSAML Certificate:"
        mtwilson_saml_cert_report
        ;;
  uninstall)
    shift
    mtwilson_stop
    mtwilson_uninstall $*
    ;;
  *)
    if [ -z "$*" ]; then
      print_help
    else
      #echo "args: $*"
      result=$($JAVA_CMD $JAVA_OPTS com.intel.mtwilson.launcher.console.Main $*)
      result_exit_code=$?
      IFS=
      echo $result | grep -vE "^\[EL Info\]|^\[EL Warning\]"
      exit $result_exit_code
    fi
    ;;
esac


exit $?
