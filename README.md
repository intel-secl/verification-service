# Intel<sup>®</sup> Security Libraries for Data Center  - Verification Service
#### The Intel<sup>®</sup> SecL - DC Verification Service component performs remote attestation of physical servers, comparing Intel<sup>®</sup> TXT measurements of BIOS, OS, Asset Tag, and other components against a database of known-good values. The attested trust status of each server is used to make policy decisions for workload placement. As a server boots, Intel<sup>®</sup> TXT begins extending measurements to a Trusted Platform Module (TPM). Each chain of trust component is measured, and these measurements are remotely verified using the Attestation Server.

## Key features
- Remote attestation of platforms
- Provides storage for good known values for platforms
- Provides trust status evaluation of platforms against good known values
- RESTful APIs for easy and versatile access to above features

## System Requirements
- RHEL 7.5/7.6
- Epel 7 Repo
- Proxy settings if applicable

## Software requirements
- git
- maven (v3.3.1)
- ant (v1.9.10 or more)

# Step By Step Build Instructions
## Install required shell commands
Please make sure that you have the right `http proxy` settings if you are behind a proxy
```shell
export HTTP_PROXY=http://<proxy>:<port>
export HTTPS_PROXY=https://<proxy>:<port>
```
### Install tools from `yum`
```shell
$ sudo yum install -y wget git zip unzip ant gcc patch gcc-c++ trousers-devel openssl-devel makeself
```

## Direct dependencies
Following repositories needs to be build before building this repository,

| Name                       | Repo URL                                                 |
| -------------------------- | -------------------------------------------------------- |
| external-artifacts         | https://github.com/intel-secl/external-artifacts         |
| contrib                    | https://github.com/intel-secl/contrib                    |
| tpm-tools-windows          | https://github.com/intel-secl/tpm-tools-windows          |
| common-java                | https://github.com/intel-secl/common-java                |
| lib-common                 | https://github.com/intel-secl/lib-common                 |
| lib-privacyca              | https://github.com/intel-secl/lib-privacyca              |
| lib-tpm-provider           | https://github.com/intel-secl/lib-tpm-provider           |
| lib-platform-info          | https://github.com/intel-secl/lib-platform-info          |
| lib-host-connector         | https://github.com/intel-secl/lib-host-connector         |
| lib-asset-tag-creator      | https://github.com/intel-secl/lib-asset-tag-creator      |
| lib-asset-tag-provisioner  | https://github.com/intel-secl/lib-asset-tag-provisioner  |
| lib-flavor                 | https://github.com/intel-secl/lib-flavor                 |
| lib-verifier               | https://github.com/intel-secl/lib-verifier               |
| lib-saml                   | https://github.com/intel-secl/lib-saml                   |
| privacyca                  | https://github.com/intel-secl/privacyca                  |
| attestation-hub            | https://github.com/intel-secl/attestation-hub            |

## Build Verification Service

- Git clone the `Verification Service`
- Run scripts to build the `Verification Service`

```shell
$ git clone https://github.com/intel-secl/verification-service.git
$ cd verification-service
$ ant
```

# Links
 - Use [Automated Build Steps](https://01.org/intel-secl/documentation/build-installation-scripts) to build all repositories in one go, this will also provide provision to install prerequisites and would handle order and version of dependent repositories.

***Note:** Automated script would install a specific version of the build tools, which might be different than the one you are currently using*
 - [Product Documentation](https://01.org/intel-secl/documentation/intel%C2%AE-secl-dc-product-guide)
