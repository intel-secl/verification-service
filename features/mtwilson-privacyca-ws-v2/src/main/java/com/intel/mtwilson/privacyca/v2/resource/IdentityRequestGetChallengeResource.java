/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.v2.resource;

import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.core.common.tpm.model.IdentityProofRequest;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeRequest;
import com.intel.mtwilson.privacyca.v2.rpc.IdentityRequestGetChallenge;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
@V2
@Path("/privacyca/identity-challenge-request")
public class IdentityRequestGetChallengeResource {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(IdentityRequestGetChallengeResource.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public IdentityProofRequest identityChallengeRequest(IdentityChallengeRequest identityChallengeRequest) throws Exception {
        IdentityRequestGetChallenge rpc = new IdentityRequestGetChallenge();
        rpc.setIdentityRequest(identityChallengeRequest.getIdentityRequest());
        rpc.setEndorsementCertificate(identityChallengeRequest.getEndorsementCertificate());
        return rpc.call();
    }

}
