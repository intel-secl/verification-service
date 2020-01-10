# RPM SPEC for ISecL Verification Service
%global mtwilson_home      %{_datadir}/%{name}
%global mtwilson_conf      %{_sysconfdir}/%{name}
%global mtwilson_env_d     %{mtwilson_conf}/env.d
%global mtwilson_var       %{mtwilson_home}/var
%global mtwilson_profile_d %{mtwilson_conf}/profile.d
%global mtwilson_tmp       %{mtwilson_var}/tmp
%global mtwilson_java      %{_javadir}/%{name}
%global mtwilson_log       %{_localstatedir}/log/%{name}
%global mtwilson_username  mtwilson

%define debug_package %{nil}
%define __jar_repack %{nil}

#Verification Service Log Rotation Defaults
%define mtwilson_log_rotation_period monthly
%define mtwilson_log_compress        compress
%define mtwilson_log_delaycompress   delaycompress
%define mtwilson_log_copytruncate    copytruncate
%define mtwilson_log_size            1G
%define mtwilson_log_old             12

#Verification Service Default Ports
%define mtwilson_port_http 8442
%define mtwilson_port_https 8443

#Verification Service RPM Metadata
Name:           host-verification-service
Version:        4.5
Release:        1%{?dist}
Summary:        RPM Package for ISecL Host Verification Service Component
Group:          Applications/System
License:        BSD-3-Clause
URL:            https://github.com/intel-secl/verification-service.git
Source0:        verification-service.tar.gz
Source1:        common-libs-hvs.tar.gz
Source2:        version
BuildRoot:      %{_tmppath}/%{name}-%{version}-%{release}-root-%(%{__id_u} -n)
BuildArch:      x86_64
ExclusiveArch:  x86_64

#Dependencies to Build Verification Service RPM from Sources
%if 0%{?centos}
BuildRequires: rh-maven35
BuildRequires: epel-release
%endif

%if 0%{?rhel}
BuildRequires: maven
BuildRequires: epel-release
%endif

%if 0%{?fedora}
BuildRequires: maven
%endif

BuildRequires:     wget
#BuildRequires:     git
BuildRequires:     zip
BuildRequires:     unzip
BuildRequires:     ant
BuildRequires:     gcc
BuildRequires:     patch
BuildRequires:     gcc-c++
BuildRequires:     trousers-devel
BuildRequires:     openssl-devel
#BuildRequires:     epel-release
BuildRequires:     makeself
BuildRequires:     rpm-build
BuildRequires:     deltarpm

#Verification Service Runtime Dependencies
Requires(pre): shadow-utils
Requires(postun): shadow-utils
Requires(post): chkconfig
Requires(preun): chkconfig
Requires(preun): initscripts
Requires(postun): initscripts
Requires:       java-1.8.0-openjdk-devel
Requires:       openssl
Requires:       openssl-devel
Requires:       logrotate
Requires:       postgresql = 11.5
Requires:       postgresql-server = 11.5
Requires:       postgresql-contrib = 11.5
Requires:       xmlstarlet
Requires:       policycoreutils
Requires:       unzip
Requires:       zip
Requires:       wget
Requires:       net-tools
Requires:       python3-policycoreutils


# Java Component Dependencies not Packaged with Verification Service
Requires:       apache-commons-beanutils = 1.9.3
Requires:       apache-commons-codec = 1.11
Requires:       apache-commons-collections = 3.2.2
Requires:       apache-commons-compress
Requires:       apache-commons-configuration = 1.10
Requires:       apache-commons-digester = 2.1
Requires:       apache-commons-exec = 1.3
Requires:       apache-commons-io
Requires:       apache-commons-lang = 2.6
Requires:       apache-commons-lang3 = 3.7
Requires:       apache-commons-logging
#Requires:       apache-commons-math3 = 3.6.1
Requires:       apache-commons-pool = 1.6
Requires:       apache-commons-validator
Requires:       apache-commons-dbcp = 1.4
Requires:       jackson-annotations
Requires:       jackson-core
Requires:       jackson-databind
Requires:       jackson-dataformat-xml
Requires:       jackson-dataformat-yaml
#Requires:       jackson-jaxrs-base = 2.9.7
Requires:       jackson-jaxrs-json-provider
Requires:       jackson-jaxrs-xml-provider
Requires:       jackson-module-jaxb-annotations
#Requires:       jetty-http
#Requires:       jetty-io
#Requires:       jetty-security
#Requires:       jetty-server
#Requires:       jetty-servlet
#Requires:       jetty-util
#Requires:       jetty-webapp
#Requires:       jetty-xml
#Requires:       jetty-continuation
#Requires:       jetty-servlet
Requires:       joda-time = 2.9.9
#Requires:       opensaml-java-core
#Requires:       opensaml-java-messaging-api
#Requires:       opensaml-java-profile-api
#Requires:       opensaml-java-saml-api
#Requires:       opensaml-java-saml-impl
#Requires:       opensaml-java-security-api
#Requires:       opensaml-java-security-impl
#Requires:       opensaml-java-soap-api
#Requires:       opensaml-java-soap-impl
#Requires:       opensaml-java-storage-api
#Requires:       opensaml-java-xmlsec-api
#Requires:       opensaml-java-xmlsec-impl
Requires:       slf4j = 1.7.25
Requires:       snakeyaml
Requires:       stax2-api
#Requires:       woodstox-core
Requires:       xstream
#Requires:       jdbi
Requires:       objenesis = 2.6
Requires:       google-gson = 2.8.2
Requires:       velocity = 1.7
Requires:       antlr = 2.7.7
Requires:       cryptacular
Requires:       dom4j
#Requires:       log4j
#Requires:       logback-core = 1.2.3
#Requires:       logback-classic = 1.2.3 
Requires:       xpp3-minimal
Requires:       objectweb-asm
Requires:       cglib
#Verification Service Provides
Provides:       %{name} = %{version}-%{release}
Provides:       mtwilson-configuration == %{version}
Provides:       mtwilson-audit-api == %{version}
Provides:       mtwilson-audit-data == %{version}
Provides:       mtwilson-audit-handler == %{version}
Provides:       mtwilson-auto-refresh-telemetry == %{version}
Provides:       mtwilson-auto-refresh-trust == %{version}
Provides:       mtwilson-certificate-client-jaxrs2 == %{version}
Provides:       mtwilson-certificate-model == %{version}
Provides:       mtwilson-certificate-ws-v2 == %{version}
Provides:       mtwilson-esxi-cluster-autoupdate == %{version}
Provides:       mtwilson-esxi-cluster-client-jaxrs2 == %{version}
Provides:       mtwilson-esxi-cluster-jdbi == %{version}
Provides:       mtwilson-esxi-cluster-model == %{version}
Provides:       mtwilson-esxi-cluster-ws-v2 == %{version}
Provides:       mtwilson-esxi-host-jdbi == %{version}
Provides:       mtwilson-file-client-jaxrs2 == %{version}
Provides:       mtwilson-file-model == %{version}
Provides:       mtwilson-file-ws-v2 == %{version}
Provides:       mtwilson-flavor-client-jaxrs2 == %{version}
Provides:       mtwilson-flavor-jpa == %{version}
Provides:       mtwilson-flavor-model == %{version}
Provides:       mtwilson-flavor-ws-v2 == %{version}
Provides:       mtwilson-my == %{version}
Provides:       mtwilson-postgresql == %{version}
Provides:       mtwilson-privacyca-setup == %{version}
Provides:       mtwilson-privacyca-ws-v2 == %{version}
Provides:       mtwilson-queue == %{version}
Provides:       mtwilson-setup == %{version}
Provides:       mtwilson-shiro-jdbi == %{version}
Provides:       mtwilson-shiro-setup == %{version}
Provides:       mtwilson-tag-client-jaxrs2 == %{version}
Provides:       mtwilson-tag-dao-jooq == %{version}
Provides:       mtwilson-tag-jdbi == %{version}
Provides:       mtwilson-tag-model == %{version}
Provides:       mtwilson-tag-selection-json == %{version}
Provides:       mtwilson-tag-selection-xml == %{version}
Provides:       mtwilson-tag-setup == %{version}
Provides:       mtwilson-tag-ws-v2 == %{version}
Provides:       mtwilson-telemetry-client-jaxrs2 == %{version}
Provides:       mtwilson-telemetry-jpa == %{version}
Provides:       mtwilson-telemetry-ws-v2 == %{version}
Provides:       mtwilson-tls-policy-client-jaxrs2 == %{version}
Provides:       mtwilson-tls-policy-jdbi == %{version}
Provides:       mtwilson-tls-policy-model == %{version}
Provides:       mtwilson-tls-policy-ws-v2 == %{version}
Provides:       mtwilson-tpm-endorsement-jdbi == %{version}
Provides:       mtwilson-tpm-endorsement-ws-v2 == %{version}
Provides:       mtwilson-user-management-client-jaxrs2 == %{version}
Provides:       mtwilson-user-management-model == %{version}
Provides:       mtwilson-user-management-ws-v2 == %{version}
Provides:       mtwilson-version == %{version}

%description
Verification Service is a ISecL component that performs remote attestation of physical servers, comparing Intel® TXT measurements of BIOS, OS, Asset Tag, and other components against a database of known-good values. The attested trust status of each server is used to make policy decisions for workload placement. As a server boots, Intel® TXT begins extending measurements to a Trusted Platform Module (TPM). Each chain of trust component is measured, and these measurements are remotely verified using the Attestation Server.

%prep
%setup -q -n verification-service
%setup -q -T -D -b 1 -n .

%build
#Build from Verification Service Sources
declare -a HVS_AH_COMMON_REPOSITORY_ORDER
HVS_AH_COMMON_REPOSITORY_ORDER=(
external-artifacts
contrib
common-java
lib-common
lib-privacyca
lib-tpm-provider
lib-platform-info
lib-host-connector
lib-flavor
lib-verifier
lib-saml
lib-asset-tag-provisioner
lib-asset-tag-creator
privacyca
verification-service
)


ant_build_repos() {
  local start_repo=${2}
  local required_to_build=false
  cat /dev/null > /tmp/ant.log
  echo "Running ant build on repositories (log file: ant.log)..."
  for repo in ${!HVS_AH_COMMON_REPOSITORY_ORDER[@]}; do
    local repo_name=${HVS_AH_COMMON_REPOSITORY_ORDER[${repo}]}
    if [ -n "${start_repo}" ] && ! $required_to_build && [ "${repo_name}" != "${start_repo}" ]; then
      echo "Skipping ant build in repository [${repo_name}]..."
      continue
    else
      required_to_build=true
    fi
    echo "Running ant build for repository [${repo_name}]..."
    (
    cd "${repo_name}"
    ant > /tmp/ant.log 2>&1
    )
    local return_code=$?
    if [ ${return_code} -ne 0 ]; then
      echo "ERROR: Issue while running build on repository [${repo_name}]"
      return ${return_code}
    fi
  done
}

ant_build_repos
if [ $? -ne 0 ]; then exit 10; fi

cp -r %{_topdir}/BUILD/verification-service/packages/host-verification-service-linux/target/host-verification-service-4.5-SNAPSHOT-rpm.tar.gz %{_topdir}/BUILD/. 

rm -rf ${HVS_AH_COMMON_REPOSITORY_ORDER[@]}

tar -xf host-verification-service-4.5-SNAPSHOT-rpm.tar.gz

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/%{_sbindir}
mkdir -p %{buildroot}/%{mtwilson_env_d}
mkdir -p %{buildroot}/%{mtwilson_home}
mkdir -p %{buildroot}/%{_sysconfdir}/%{name}
mkdir -p %{buildroot}/%{_sysconfdir}/logrotate.d
mkdir -p %{buildroot}/%{mtwilson_home}/backup
mkdir -p %{buildroot}/%{mtwilson_home}/var
mkdir -p %{buildroot}/%{mtwilson_var}/tmp
mkdir -p %{buildroot}/%{mtwilson_var}/repository
mkdir -p %{buildroot}/%{mtwilson_home}/scripts
mkdir -p %{buildroot}/%{_localstatedir}/log/%{name}
mkdir -p %{buildroot}/%{_javadir}/%{name}

mkdir -p %{buildroot}/%{mtwilson_home}/var/intel/aikverifyhome/bin
mkdir -p %{buildroot}/%{mtwilson_home}/var/intel/aikverifyhome/data
mkdir -p %{buildroot}/%{mtwilson_home}/features/tag/var
mkdir -p %{buildroot}/%{mtwilson_home}/features/tag/bin
mkdir -p %{buildroot}/%{mtwilson_home}/share
mkdir -p %{buildroot}/%{mtwilson_var}/opt/intel

#Verification Service Essential Files
%define app_target host-verification-service-%{version}-SNAPSHOT
unzip -o host-verification-service-%{version}-SNAPSHOT/host-verification-service-zip-%{version}-SNAPSHOT-application.zip
cp configuration/*       %{buildroot}/%{mtwilson_conf}
cp java/*                %{buildroot}/%{_javadir}/%{name}
cp -r features/mtwilson-configuration-settings-ws-v2 %{buildroot}/%{mtwilson_home}/features
cp -r features/mtwilson-core-version %{buildroot}/%{mtwilson_home}/features
cp -r features/servlet3  %{buildroot}/%{mtwilson_home}/features
cp -r share/*            %{buildroot}/%{mtwilson_home}/share
cp %{app_target}/functions             %{buildroot}/%{mtwilson_home}/scripts/functions.sh
cp %SOURCE2                            %{buildroot}/%{mtwilson_home}/scripts/version.sh
cp %{app_target}/log4j.properties      %{buildroot}/%{mtwilson_conf}
cp %{app_target}/logback-stderr.xml    %{buildroot}/%{mtwilson_conf}
cp %{app_target}/logback.xml           %{buildroot}/%{mtwilson_conf}
cp %{app_target}/mylogrotate.te        %{buildroot}/%{mtwilson_conf}

cp %{app_target}/shiro-localhost.ini   %{buildroot}/%{mtwilson_conf}
cp %{app_target}/shiro.ini             %{buildroot}/%{mtwilson_conf}
cp %{app_target}/java.security         %{buildroot}/%{mtwilson_home}/backup
cp %{app_target}/encrypt.sh            %{buildroot}/%{mtwilson_home}/features/tag/bin
cp %{app_target}/decrypt.sh            %{buildroot}/%{mtwilson_home}/features/tag/bin
cp %SOURCE2                            %{buildroot}/%{mtwilson_conf}/version
cp %{app_target}/extdeps.xml           %{buildroot}/%{mtwilson_conf}/extdeps.xml
cp %{app_target}/mtwilson.sh           %{buildroot}/%{_sbindir}/mtwilson.sh
cp %{app_target}/encrypt.sh            %{buildroot}/%{_sbindir}/encrypt.sh
cp %{app_target}/decrypt.sh            %{buildroot}/%{_sbindir}/decrypt.sh

touch %{buildroot}/%{mtwilson_log}/mtwilson.log
touch %{buildroot}/%{mtwilson_log}/mtwilson-log4j.log
touch %{buildroot}/%{mtwilson_log}/mtwilson-audit.log
touch %{buildroot}/%{mtwilson_log}/install.log

%pre
# Load Application Environment Variables if Already Defined
if [ -d %{mtwilson_env_d} ]; then
  MTWILSON_ENV_FILES=$(ls -1 %{mtwilson_env_d}/*)
  for env_file in $MTWILSON_ENV_FILES; do
    . $env_file
    env_file_exports=$(cat $env_file | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
    if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
  done
fi

#  Check for Verification Service Environment Configuration
if [ -f ~/mtwilson.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/mtwilson.env" 
  . ~/mtwilson.env
  env_file_exports=$(cat ~/mtwilson.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
  exit 1
fi

# If an Existing MtWilson is Already Running, Stop it While We Install
echo "Checking for previously installed Mt Wilson..." 
prev_mtwilson="$(which mtwilson 2>/dev/null)"
if [ -n "$prev_mtwilson" ] && [ "$(whoami)" == "root" ]; then
  # stop mtwilson; this sometimes does not work
  $prev_mtwilson stop
  echo "After '$prev_mtwilson stop', checking status again..." 
  $prev_mtwilson status 
  # remove previous mtwilson script
  rm -f $prev_mtwilson
fi

# Create a MtWilson User if There Isn't Already One Created
if ! getent passwd %{mtwilson_username} >/dev/null 2>&1; then
   useradd --comment "Mt Wilson" --system --shell /bin/false %{mtwilson_username}
   usermod --lock %{mtwilson_username}
   # note: to assign a shell and allow login you can run "usermod --shell /bin/bash --unlock $MTWILSON_USERNAME"
fi


%post

# Load Installer Environment File, if Present
if [ -f ~/mtwilson.env ]; then
  echo "Loading environment variables from $(cd ~ && pwd)/mtwilson.env" >> %{_localstatedir}/log/%{name}/install.log
  . ~/mtwilson.env
  env_file_exports=$(cat ~/mtwilson.env | grep -E '^[A-Z0-9_]+\s*=' | cut -d = -f 1)
  if [ -n "$env_file_exports" ]; then eval export $env_file_exports; fi
else
  echo "No environment file"
fi

# Setup Log File Location
export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-%{mtwilson_log}/install.log}

. %{mtwilson_home}/scripts/functions.sh  >/dev/null

# Store Directory Layout in Env File
echo "# $(date)" > %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_HOME=%{mtwilson_home}" >> %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_CONFIGURATION=%{mtwilson_conf}" >> %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_JAVA=%{mtwilson_java}" >> %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_BIN=%{_sbindir}" >> %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_REPOSITORY=%{mtwilson_var}/repository" >> %{mtwilson_env_d}/mtwilson-layout
echo "export MTWILSON_LOGS=%{mtwilson_log}" >> %{mtwilson_env_d}/mtwilson-layout

# Store MtWilson Username in Env File
echo "# $(date)" > %{mtwilson_env_d}/mtwilson-username
echo "export MTWILSON_USERNAME=%{mtwilson_username}" >> %{mtwilson_env_d}/mtwilson-username

# Store Log Level in Env File, if it's Set
if [ -n "$MTWILSON_LOG_LEVEL" ]; then
  echo "# $(date)" > %{mtwilson_env_d}/mtwilson-logging
  echo "export MTWILSON_LOG_LEVEL=$MTWILSON_LOG_LEVEL" >> %{mtwilson_env_d}/mtwilson-logging
fi

echo "# $(date)" > %{mtwilson_env_d}/mtwilson-setup
for env_file_var_name in $env_file_exports
do
  eval env_file_var_value="\$$env_file_var_name"
  echo "writing $env_file_var_name to mtwilson-setup with value: $env_file_var_value" >> %{_localstatedir}/log/%{name}/install.log
  echo "$env_file_var_name=$env_file_var_value" >> %{mtwilson_env_d}/mtwilson-setup
done

#Export Verification Service Environment Variables
export MTWILSON_HOME=%{mtwilson_home}
export MTWILSON_CONFIGURATION=%{mtwilson_conf}
export MTWILSON_JAVA=%{mtwilson_java}
export MTWILSON_BIN=%{_sbindir}
export MTWILSON_LOGS=%{mtwilson_log}
export MTWILSON_USERNAME=%{mtwilson_username}
export MTWILSON_DEFAULT_TLS_POLICY_ID=${MTWILSON_DEFAULT_TLS_POLICY_ID:-"TRUST_FIRST_CERTIFICATE"}
export MTWILSON_ENV=%{mtwilson_env_d}
export INSTALL_LOG_FILE=${INSTALL_LOG_FILE:-%{mtwilson_log}/install.log}

export POSTGRES_USERNAME=${DATABASE_USERNAME}
export POSTGRES_PASSWORD=${DATABASE_PASSWORD}
export DATABASE_HOSTNAME=${DATABASE_HOSTNAME:-127.0.0.1}
export DATABASE_PORTNUM=${DATABASE_PORTNUM:-5432}
export DATABASE_SCHEMA=${DATABASE_SCHEMA:-mw_as}
export DATABASE_VENDOR=postgres
export POSTGRES_HOSTNAME=${DATABASE_HOSTNAME}
export POSTGRES_PORTNUM=${DATABASE_PORTNUM}
export POSTGRES_DATABASE=${DATABASE_SCHEMA}
export POSTGRESQL_KEEP_PGPASS=${POSTGRESQL_KEEP_PGPASS:-true}

export MTWILSON_TLS_POLICY_ALLOW=${MTWILSON_TLS_POLICY_ALLOW:-"certificate,certificate-digest,public-key,public-key-digest,TRUST_FIRST_CERTIFICATE,INSECURE"}
export JAVA_REQUIRED_VERSION=${JAVA_REQUIRED_VERSION:-1.8}
export INSTALLED_MARKER_FILE=${INSTALLED_MARKER_FILE:-%{mtwilson_var}/opt/intel/.mtwilsonInstalled}
export PRIVACYCA_DOWNLOAD_USERNAME=${PRIVACYCA_DOWNLOAD_USERNAME:-pca-admin}
export PRIVACYCA_DOWNLOAD_PASSWORD=${PRIVACYCA_DOWNLOAD_PASSWORD:-PrivacyCaPassword}
export POSTGRES_REQUIRED_VERSION=${POSTGRES_REQUIRED_VERSION:-9.4}
export DATABASE_VENDOR=${DATABASE_VENDOR:-postgres}
export ADD_POSTGRESQL_REPO=${ADD_POSTGRESQL_REPO:-yes}
export MTWILSON_API_SSL_VERIFY_HOSTNAME=${MTWILSON_API_SSL_VERIFY_HOSTNAME:-false}
export PRIVACYCA_KEYGEN_URANDOM=${PRIVACYCA_KEYGEN_URANDOM:-yes}
export MTWILSON_TAG_ADMIN_USERNAME=${MTWILSON_TAG_ADMIN_USERNAME:-tagentadmin}
export MTWILSON_TAG_ADMIN_PASSWORD=${MTWILSON_TAG_ADMIN_PASSWORD:-TagProvisionPassword}
export TAG_PROVISION_NOCACHE=${TAG_PROVISION_NOCACHE:-true}

#Set Verification Service Ports
MTWILSON_PORT_HTTP=${MTWILSON_PORT_HTTP:-${JETTY_PORT:-8442}}
MTWILSON_PORT_HTTPS=${MTWILSON_PORT_HTTPS:-${JETTY_SECURE_PORT:-8443}}

rm -f $MTWILSON_HOME/bin/mtwilson
ln -s %{_sbindir}/mtwilson.sh %{_sbindir}/mtwilson

# Define Directory Layout
chown $MTWILSON_USERNAME:$MTWILSON_USERNAME $INSTALL_LOG_FILE
date > $INSTALL_LOG_FILE

set_owner_for_mtwilson_directories() {
  for directory in $MTWILSON_HOME $MTWILSON_JAVA $MTWILSON_ENV $MTWILSON_REPOSITORY $MTWILSON_LOGS; do
    chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME $directory
  done
}
set_owner_for_mtwilson_directories


#Validate Installation of Database and Configure
if [[ -z "$opt_postgres" && -z "$opt_mysql" ]]; then
 echo_warning "Relying on an existing database installation"
fi

if using_postgres; then
  postgres_installed=1
  setup_pgpass
fi

postgres_server_rpm %{mtwilson_conf}

if using_postgres; then
  if [ -z "$SKIP_DATABASE_INIT" ]; then
    # postgres db init here
    postgres_create_database
    if [ $? -ne 0 ]; then
      echo_failure "Cannot create database"
      exit 1
    fi
    #postgres_restart >> $INSTALL_LOG_FILE
    #sleep 10
    #export is_postgres_available postgres_connection_error
    if [ -z "$is_postgres_available" ]; then
      echo_warning "Run 'mtwilson setup' after a database is available";
    fi
    # postgress db init end
  else
    echo_warning "Skipping init of database"
  fi
  if [ $postgres_installed -eq 0 ]; then
    postgres_server_detect
    has_local_postgres_peer=`grep "^local.*all.*postgres.*peer" $postgres_pghb_conf`
    if [ -z "$has_local_postgres_peer" ]; then
      echo "Adding PostgreSQL local 'peer' authentication for 'postgres' user..."
      sed -i '/^.*TYPE.*DATABASE.*USER.*ADDRESS.*METHOD/a local all postgres peer' $postgres_pghb_conf
    fi
    has_local_peer=`grep "^local.*all.*all.*peer" $postgres_pghb_conf`
    if [ -n "$has_local_peer" ]; then
      echo "Replacing PostgreSQL local 'peer' authentication with 'md5' authentication..."
      sed -i 's/^local.*all.*all.*peer/local all all md5/' $postgres_pghb_conf
    fi
    has_max_connections=`grep "^max_connections" $postgres_conf`
    if [ -n "$has_max_connections" ]; then
      postgres_max_connections=$(cat "$postgres_conf" 2>/dev/null | grep "^max_connections" | head -n 1 | sed 's/#.*//' | awk -F '=' '{ print $2 }' | sed -e 's/^[ \t]*//' | sed -e 's/[ \t]*$//')
      if [ -z $postgres_max_connections ] || [ $postgres_max_connections -lt 400 ]; then
        echo "Changing postgresql configuration to set max connections to 400...";
        sed -i 's/^max_connections.*/max_connections = 400/' $postgres_conf
      fi
    else
      echo "Setting postgresql max connections to 400...";
	  echo "max_connections = 400" >> $postgres_conf
    fi
    has_shared_buffers=`grep "^shared_buffers" $postgres_conf`
    if [ -n "$has_shared_buffers" ]; then
      echo "Changing postgresql configuration to set shared buffers size to 400MB...";
      sed -i 's/^shared_buffers.*/shared_buffers = 400MB/' $postgres_conf
    else
      echo "Setting postgresql shared buffers size to 400MB...";
      echo "shared_buffers = 400MB" >> $postgres_conf
    fi
    if [ "$POSTGRESQL_KEEP_PGPASS" != "true" ]; then
      if [ -f ${MTWILSON_CONFIGURATION}/.pgpass ]; then
        echo "Removing .pgpass file to prevent insecure database password storage in plaintext..."
        rm -f ${MTWILSON_CONFIGURATION}/.pgpass
        if [ $(whoami) == "root" ]; then rm -f ~/.pgpass; fi
      fi
    fi
    postgres_restart >> %{_localstatedir}/log/%{name}/install.log
  fi
fi

#Backup Existing Configuration	  
mtwilson_backup_configuration() {
  if [ -n "$MTWILSON_CONFIGURATION" ] && [ -d "$MTWILSON_CONFIGURATION" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir="$MTWILSON_HOME/backup/mtwilson.configuration.$datestr"
    mkdir -p "$backupdir"
    cp -r $MTWILSON_CONFIGURATION $backupdir
  fi
}

mtwilson_backup_repository() {
  if [ -n "$MTWILSON_REPOSITORY" ] && [ -d "$MTWILSON_REPOSITORY" ]; then
    datestr=`date +%Y%m%d.%H%M`
    backupdir="$MTWILSON_HOME/backup/mtwilson.repository.$datestr"
    mkdir -p "$backupdir"
    cp -r $MTWILSON_REPOSITORY $backupdir
  fi
}

mtwilson_backup_configuration
mtwilson_backup_repository

#Setup MtWilson Profile
profile_dir=$MTWILSON_HOME
if [ "$(whoami)" == "root" ] && [ -n "%{mtwilson_username}" ] && [ "%{mtwilson_username}" != "root" ]; then
  profile_dir=%{mtwilson_home}
fi
profile_name=$profile_dir/$(basename $(getUserProfileFile))

echo "Updating profile: $profile_name" >> %{_localstatedir}/log/%{name}/install.log
appendToUserProfileFile "export PATH=%{_sbindir}:\$PATH" $profile_name
appendToUserProfileFile "export MTWILSON_HOME=%{mtwilson_home}" $profile_name

#Set MtWilson Service Properties
mtw_props_path="%{mtwilson_conf}/mtwilson.properties"
as_props_path="%{mtwilson_conf}/attestation-service.properties"
file_paths=("$mtw_props_path" "$as_props_path")

mtwilson_password_file="%{mtwilson_conf}/.mtwilson_password"

if [ -f "$mtwilson_password_file" ]; then
  export MTWILSON_PASSWORD=$(cat $mtwilson_password_file)
fi


# Java Configuration
JAVA_CMD=$(type -p java | xargs readlink -f)
JAVA_HOME=$(dirname $JAVA_CMD | xargs dirname | xargs dirname)
JAVA_REQUIRED_VERSION=$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $2}')
# store java location in env file
echo "# $(date)" > %{mtwilson_env_d}/mtwilson-java
echo "export JAVA_HOME=$JAVA_HOME" >> %{mtwilson_env_d}/mtwilson-java
echo "export JAVA_CMD=$JAVA_CMD" >> %{mtwilson_env_d}/mtwilson-java
echo "export JAVA_REQUIRED_VERSION=$JAVA_REQUIRED_VERSION" >> %{mtwilson_env_d}/mtwilson-java

#chmod 600 %{mtwilson_conf}/env.d/*

if [ -f "${JAVA_HOME}/jre/lib/security/java.security" ]; then
  echo "Replacing java.security file, existing file will be backed up"
  backup_file "${JAVA_HOME}/jre/lib/security/java.security"
  cp %{mtwilson_home}/backup/java.security "${JAVA_HOME}/jre/lib/security/java.security"
fi

# if a group is defined, then make all sub parts == true
if [ ! -z "$opt_services" ]; then
  eval opt_attservice="true"
  eval opt_mangservice="true"
  eval opt_wlmservice="true"
fi

# configure mtwilson TLS policies
echo "Configuring TLS policies..." >>%{_localstatedir}/log/%{name}/install.log
if [ -f "%{mtwilson_conf}/mtwilson.properties" ]; then
  #default_mtwilson_tls_policy_id="$MTWILSON_DEFAULT_TLS_POLICY_ID"
  default_mtwilson_tls_policy_id="${MTWILSON_DEFAULT_TLS_POLICY_ID:-$MTW_DEFAULT_TLS_POLICY_ID}"   #`read_property_from_file "mtwilson.default.tls.policy.id" /etc/intel/cloudsecurity/mtwilson.properties`
  if [ "$default_mtwilson_tls_policy_id" == "INSECURE" ] || [ "$default_mtwilson_tls_policy_id" == "TRUST_FIRST_CERTIFICATE" ]; then
    echo_warning "Default TLS policy is insecure; the product guide contains information on enabling secure TLS policies"
  fi
  #fi
  export MTWILSON_TLS_KEYSTORE_PASSWORD="${MTWILSON_TLS_KEYSTORE_PASSWORD:-$MTW_TLS_KEYSTORE_PASS}"   #`read_property_from_file "mtwilson.tls.keystore.password" /etc/intel/cloudsecurity/mtwilson.properties`
else
  touch "%{mtwilson_conf}/mtwilson.properties"
  chmod 600 "%{mtwilson_conf}/mtwilson.properties"
  chown %{mtwilson_username}:%{mtwilson_username} "%{mtwilson_conf}/mtwilson.properties"
  export MTWILSON_TLS_KEYSTORE_PASSWORD=`generate_password 32`
  echo '#mtwilson.default.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  "%{mtwilson_conf}/mtwilson.properties"
  echo '#mtwilson.global.tls.policy.id=uuid of a shared policy or INSECURE or TRUST_FIRST_CERTIFICATE for Mt Wilson 1.2 behavior' >>  "%{mtwilson_conf}/mtwilson.properties"
  # NOTE: do not change this property once it exists!  it would lock out all hosts that are already added and prevent mt wilson from getting trust status
  # in a future release we will have a UI mechanism to manage this.
fi

MTWILSON_TLS_POLICY_ALLOW=`echo $MTWILSON_TLS_POLICY_ALLOW | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTWILSON_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTWILSON_TLS_POLICY_ALLOW=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "certificate" ] || [ "$i" == "certificate-digest" ] || [ "$i" == "public-key" ] || [ "$i" == "public-key-digest" ] || [ "$i" == "TRUST_FIRST_CERTIFICATE" ] || [ "$i" == "INSECURE" ]; then
    TMP_MTWILSON_TLS_POLICY_ALLOW+="$i,"
  fi
done
MTWILSON_TLS_POLICY_ALLOW=`echo "$TMP_MTWILSON_TLS_POLICY_ALLOW" | sed 's/\(.*\),/\1/'`

if [ -n "$MTWILSON_TLS_POLICY_ALLOW" ]; then
  mtwilson config "mtwilson.tls.policy.allow" "$MTWILSON_TLS_POLICY_ALLOW" >/dev/null
else
  echo_failure "An allowed TLS policy must be defined."
  exit -1
fi

MTWILSON_DEFAULT_TLS_POLICY_ID=`echo $MTWILSON_DEFAULT_TLS_POLICY_ID | tr -d ' '`   # trim whitespace
OIFS=$IFS
IFS=',' read -ra POLICIES <<< "$MTWILSON_TLS_POLICY_ALLOW"
IFS=$OIFS
TMP_MTWILSON_DEFAULT_TLS_POLICY_ID=
for i in "${POLICIES[@]}"; do
  if [ "$i" == "$MTWILSON_DEFAULT_TLS_POLICY_ID" ]; then
    TMP_MTWILSON_DEFAULT_TLS_POLICY_ID="$i"
  fi
done
MTWILSON_DEFAULT_TLS_POLICY_ID=`echo "$TMP_MTWILSON_DEFAULT_TLS_POLICY_ID"`

if [[ "$MTWILSON_DEFAULT_TLS_POLICY_ID" == "INSECURE" || "$MTWILSON_DEFAULT_TLS_POLICY_ID" == "TRUST_FIRST_CERTIFICATE" ]]; then
  mtwilson config "mtwilson.default.tls.policy.id" "$MTWILSON_DEFAULT_TLS_POLICY_ID" >/dev/null
else
  echo_warning "Unable to determine default TLS policy."
fi


# Update Configurations
mtwilson config "mtwilson.db.host" "${DATABASE_HOSTNAME}" >/dev/null
mtwilson config "mtwilson.db.port" "${DATABASE_PORTNUM}" >/dev/null
mtwilson config "mtwilson.db.schema" "${DATABASE_SCHEMA}" >/dev/null
mtwilson config "mtwilson.db.user" "${DATABASE_USERNAME}" >/dev/null
mtwilson config "mtwilson.db.password" "${DATABASE_PASSWORD}" >/dev/null
mtwilson config "mtwilson.locales" "en-US" >/dev/null

# Queue Execution Interval configuration parameter (in seconds)
export QUEUE_EXECUTION_INTERVAL=${QUEUE_EXECUTION_INTERVAL:-3}
mtwilson config "mtwilson.queue.execution.interval" "$QUEUE_EXECUTION_INTERVAL" >/dev/null

# Client connection timeout configuration parameter (in seconds)
export CLIENT_CONNECTION_TIMEOUT=${CLIENT_CONNECTION_TIMEOUT:-10}
mtwilson config "mtwilson.config.client.connectTimeout" "$CLIENT_CONNECTION_TIMEOUT" >/dev/null

# Client read timeout configuration parameter (in seconds)
export CLIENT_READ_TIMEOUT=${CLIENT_READ_TIMEOUT:-10}
mtwilson config "mtwilson.config.client.readTimeout" "$CLIENT_READ_TIMEOUT" >/dev/null

# ESXI Host Auto Registration Interval configuration parameter (in seconds)
export ESXIHOSTS_AUTOUPDATE_INTERVAL=${ESXIHOSTS_AUTOUPDATE_INTERVAL:-120}
mtwilson config "mtwilson.esxihosts.autoupdate.interval" "$ESXIHOSTS_AUTOUPDATE_INTERVAL" >/dev/null

# Telemetry Interval configuration parameter (in seconds) 86400s = 1 day
export MTWILSON_TELEMETRY_INTERVAL=${MTWILSON_TELEMETRY_INTERVAL:-86400}
mtwilson config "mtwilson.telemetry.interval" "$MTWILSON_TELEMETRY_INTERVAL" >/dev/null

# Host-Verification Service audit log table size configuration parameter
export MTWILSON_AUDIT_LOG_MAX_ROW_COUNT=${MTWILSON_AUDIT_LOG_MAX_ROW_COUNT:-1000000}
mtwilson config "mtwilson.audit.log.max.row.count" "$MTWILSON_AUDIT_LOG_MAX_ROW_COUNT" >/dev/null

# Host-Verification Service audit log number of rotations configuration parameter
export MTWILSON_AUDIT_LOG_NUM_ROTATIONS=${MTWILSON_AUDIT_LOG_NUM_ROTATIONS:-10}
mtwilson config "mtwilson.audit.log.num.rotations" "$MTWILSON_AUDIT_LOG_NUM_ROTATIONS" >/dev/null

# SAML configuration parameters (these are static ones). The issuer and passwords
# are written by asctl.sh
export SAML_KEYSTORE_FILE=${SAML_KEYSTORE_FILE:-SAML.p12}
mtwilson config "saml.keystore.file" "$SAML_KEYSTORE_FILE" >/dev/null
export SAML_KEY_ALIAS=${SAML_KEY_ALIAS:-samlkey1}
mtwilson config "saml.key.alias" "$SAML_KEY_ALIAS" >/dev/null
export SAML_VALIDITY_SECONDS=${SAML_VALIDITY_SECONDS:-86400}
mtwilson config "saml.validity.seconds" "$SAML_VALIDITY_SECONDS" >/dev/null

#Save variables to properties file
postgres_write_connection_properties "%{mtwilson_conf}/mtwilson.properties" mtwilson.db

# default connection pool settings
update_property_in_file "dbcp.validation.query" "%{mtwilson_conf}/mtwilson.properties" "select 1"
mtwilson config "dbcp.validation.on.borrow" "true" >/dev/null
mtwilson config "dbcp.validation.on.return" "false" >/dev/null

# Gather default configuration
MTWILSON_SERVER_IP_ADDRESS=${MTWILSON_SERVER_IP_ADDRESS:-$(hostaddress)}

# validate mtwilson server
for h in $(hostaddress_list); do echo "+ $h"; done; echo "+ "`hostname`
if [ -z "MTWILSON_SERVER" ]; then
  echo_warning "MTWILSON_SERVER details missing in properties";
  exit -1;
fi
export MTWILSON_SERVER
echo

# Attestation service auto-configuration
export PRIVACYCA_SERVER=$MTWILSON_SERVER

# copy shiro.ini api security file
#if [ ! -f "$MTWILSON_CONFIGURATION/shiro.ini" ]; then
#  echo "Copying shiro.ini to $MTWILSON_CONFIGURATION" >> %{_localstatedir}/log/%{name}/install.log
#  chmod 600 shiro.ini shiro-localhost.ini
#  cp shiro.ini shiro-localhost.ini "$MTWILSON_CONFIGURATION"
#fi
chown -R $MTWILSON_USERNAME:$MTWILSON_USERNAME "${MTWILSON_CONFIGURATION}"

echo "Adding $MTWILSON_SERVER to shiro.ini..." >>%{_localstatedir}/log/%{name}/install.log
# add MTWILSON_SERVER to shiro trust file
# use "hostFilter.allow" when using the access-denying filter (any clients not from that list of ip's will be denied)
# use "iniHostRealm.allow" when using the access-allowing filter (any clients from that list of ip's will be allowed access but clients from other ip's can still try password or x509 authentication) - this is the current default
hostAllowPropertyName=iniHostRealm.allow
sed -i '/'"$hostAllowPropertyName"'/ s/^#//g' "%{mtwilson_conf}/shiro.ini"
hostAllow=`read_property_from_file $hostAllowPropertyName "%{mtwilson_conf}/shiro.ini"`
if [[ $hostAllow != *$MTWILSON_SERVER* ]]; then
  update_property_in_file "$hostAllowPropertyName" "%{mtwilson_conf}/shiro.ini" "$hostAllow,$MTWILSON_SERVER"
fi
hostAllow=`read_property_from_file $hostAllowPropertyName "%{mtwilson_conf}/shiro.ini"`
if [[ $hostAllow != *$MTWILSON_SERVER_IP_ADDRESS* ]]; then
  update_property_in_file "$hostAllowPropertyName" "%{mtwilson_conf}/shiro.ini" "$hostAllow,$MTWILSON_SERVER_IP_ADDRESS"
fi
sed -i '/'"$hostAllowPropertyName"'/ s/^\([^#]\)/#\1/g' "%{mtwilson_conf}/shiro.ini"

# This property is needed by the UpdateSslPort command to determine the port # that should be used in the shiro.ini file
MTWILSON_API_BASEURL="https://${MTWILSON_SERVER}:$mtwilson_port_https/mtwilson/v1"
mtwilson config "mtwilson.api.url" "$MTWILSON_API_BASEURL" >/dev/null

# Make sure the nodeploy flag is cleared, so service setup commands will deploy their .war files
export MTWILSON_SETUP_NODEPLOY=

# Configure tag
export MTWILSON_TAG_CERT_IMPORT_AUTO=${MTWILSON_TAG_CERT_IMPORT_AUTO:-true}
export TAG_VALIDITY_SECONDS=${TAG_VALIDITY_SECONDS:-31536000}
mtwilson config "tag.validity.seconds" "31536000" >/dev/null
export TAG_ISSUER_DN=${TAG_ISSUER_DN:-'CN=mtwilson-tag-ca'}
mtwilson config "tag.issuer.dn" "CN=mtwilson-tag-ca" >/dev/null

mtwilson setup update-extensions-cache-file --force 2> /dev/null

if [ -z "$MTWILSON_NOSETUP" ]; then
  mtwilson setup || true
fi

mtwilson start

#  config logrotate
if [ ! -a %{_sysconfdir}/logrotate.d/mtwilson ]; then
 echo "/opt/mtwilson/logs/mtwilson.log {
    missingok
        notifempty
        rotate %{mtwilson_log_old}
        maxsize %{mtwilson_log_size}
    nodateext
        %{mtwilson_log_rotation_period}
        %{mtwilson_log_compress}
        %{mtwilson_log_delaycompress}
        %{mtwilson_log_copytruncate}
}" > %{_sysconfdir}/logrotate.d/mtwilson
fi

checkmodule -M -m %{mtwilson_conf}/mylogrotate.te -o /tmp/mylogrotate.mod
semodule_package -m /tmp/mylogrotate.mod -o /tmp/mylogrotate.pp
semodule -i /tmp/mylogrotate.pp
rm -rf /tmp/mylogrotate.mod /tmp/mylogrotate.pp

find %{mtwilson_conf}/ -type f -exec chmod 600 {} \;
chmod 600 %{mtwilson_log}/intel.postgres.err

# store server hostname or ip address (whatever user configured) for server
# to use when constructing self-references
mtwilson config mtwilson.host "$MTWILSON_SERVER" >/dev/null

# delete the temporary setup environment variables file
rm -f $MTWILSON_ENV/mtwilson-setup
rm -f ~/.pgpass

MTWILSON_PID_FILE=%{mtwilson_var}/mtwilson.pid

register_startup_script %{_sbindir}/mtwilson mtwilson $MTWILSON_PID_FILE

echo "Restarting webservice for all changes to take effect"
mtwilson restart

echo "Log file for install is located at %{mtwilson_log}/install.log"
if [ -n "$INSTALLED_MARKER_FILE" ]; then
 touch $INSTALLED_MARKER_FILE
fi
if [ "$(whoami)" != "root" ]; then
 echo_warning "Please relogin to use mtwilson utilities"
fi

hash -r mtwilson >/dev/null 2>&1

%clean
rm -rf %{buildroot}

%files
%defattr(0755,%{mtwilson_username},%{mtwilson_username},0700)

%{mtwilson_conf}
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_conf}
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_conf}/env.d

%{mtwilson_home}
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}

%{mtwilson_home}/backup

%{mtwilson_java}
%attr(0644, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_java}/*

%{mtwilson_home}/scripts
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/scripts

%{mtwilson_home}/features/mtwilson-configuration-settings-ws-v2
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/features/mtwilson-configuration-settings-ws-v2

%{mtwilson_home}/features/servlet3
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/features/servlet3

%{mtwilson_home}/features/mtwilson-core-version
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/features/mtwilson-core-version

%{mtwilson_home}/share
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/share

%{mtwilson_home}/features/tag/var
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/features/tag/var

%{mtwilson_home}/features/tag/bin
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/features/tag/bin

%{mtwilson_var}/opt/intel
%attr(0755, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_var}/opt/intel

%attr(0700,%{mtwilson_username},%{mtwilson_username}) %{_sbindir}/mtwilson.sh
%attr(0700,%{mtwilson_username},%{mtwilson_username}) %{_sbindir}/encrypt.sh
%attr(0700,%{mtwilson_username},%{mtwilson_username}) %{_sbindir}/decrypt.sh
%attr(0700, %{mtwilson_username},%{mtwilson_username}) %{mtwilson_conf}/extdeps.xml

%attr(0700,%{mtwilson_username},%{mtwilson_username}) %{mtwilson_log}
%attr(0600,%{mtwilson_username},%{mtwilson_username}) %{mtwilson_log}/*
%attr(0755,%{mtwilson_username},%{mtwilson_username}) %{mtwilson_home}/scripts/*

%preun
mtwilson uninstall || true

%postun
if [ -d %{mtwilson_conf} ]; then
   rm -rf %{mtwilson_conf}
fi

if [ -d %{mtwilson_home} ]; then
   rm -rf %{mtwilson_home}
fi

if [ -d %{mtwilson_log} ]; then
   rm -rf %{mtwilson_log}
fi

%changelog
* Thu Sep 12 2019 Uday <uday.tejx.kommuri@intel.com>
- First release of Verification Service RPM.

