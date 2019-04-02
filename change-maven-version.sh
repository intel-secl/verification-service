#!/bin/bash

# define action usage commands
usage() { echo "Usage: $0 [-v \"version\"]" >&2; exit 1; }

# set option arguments to variables and echo usage on failures
version=
while getopts ":v:" o; do
  case "${o}" in
    v)
      version="${OPTARG}"
      ;;
    \?)
      echo "Invalid option: -$OPTARG"
      usage
      ;;
    *)
      usage
      ;;
  esac
done

if [ -z "$version" ]; then
  echo "Version not specified" >&2
  exit 2
fi

changeVersionCommand="mvn versions:set -DnewVersion=${version}"
changeParentVersionCommand="mvn versions:update-parent -DallowSnapshots=true -DparentVersion=${version}"
mvnInstallCommand="mvn clean install"

(cd features && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"features\" folder" >&2; exit 3; fi
(cd features && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions in the \"features\" folder" >&2; exit 3; fi
(cd packages && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version on \"packages\" folder" >&2; exit 3; fi
(cd packages && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven parent versions in the \"packages\" folder" >&2; exit 3; fi
(cd packages/host-verification-service-zip && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version in \"packages/host-verification-service-zip\" folder" >&2; exit 3; fi
(cd packages/host-verification-service-client-java8 && $changeParentVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version in \"packages/host-verification-service-client-java8\" folder" >&2; exit 3; fi
(cd packages/host-verification-service-docker && $changeVersionCommand)
if [ $? -ne 0 ]; then echo "Failed to change maven version in \"packages/host-verification-service-docker\" folder" >&2; exit 3; fi
sed -i 's/\-[0-9\.]*\(\-SNAPSHOT\|\(\-\|\.zip$\|\.bin$\|\.jar$\)\)/-'${version}'\2/g' build.targets
if [ $? -ne 0 ]; then echo "Failed to change versions in \"build.targets\" file" >&2; exit 3; fi
