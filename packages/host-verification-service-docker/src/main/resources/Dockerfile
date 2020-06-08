#####
#Copyright © 2018 Intel Corporation
#SPDX-License-Identifier: BSD-3-Clause
#####

FROM registry.access.redhat.com/rhel7-init:7.5

# Copy yum repositories (If there is a non-redhat repo)
COPY yum.repos.d/ /etc/yum.repos.d/

#RUN yum makecache
RUN yum -y install which sudo redhat-lsb-core systemd

# Install Postgres 9.4 client
RUN yum install https://download.postgresql.org/pub/repos/yum/9.4/redhat/rhel-7-x86_64/pgdg-redhat94-9.4-3.noarch.rpm -y && \
     yum install -y postgresql94

# Copy the scripts
COPY host-verification-service-linux*-SNAPSHOT.bin /root/hvs-linux.bin
RUN chmod +x /root/hvs-linux.bin

# Copy the installer and extract just the static components 
WORKDIR /root/

# Install Verification Service
RUN export MTWILSON_NOSETUP=yes && export SKIP_DATABASE_INIT=yes && \
    export INSTALL_PKGS="java logrotate privacyca SERVICES PORTALS" && \
    export psql=`which psql 2>/dev/null` && \
    /root/hvs-linux.bin --keep --target /root/hvs_installer &> /root/install.log

RUN chown -R mtwilson:mtwilson /opt/mtwilson

# Remove the installer bin
RUN rm -rf /root/hvs-linux.bin /root/hvs_installer

COPY start_hvs /root/
RUN chmod +x /root/start_hvs

ENTRYPOINT [ "/root/start_hvs" ]
