/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.privacyca.v2.resource;

import com.intel.mtwilson.jaxrs2.mediatype.DataMediaType;
import com.intel.mtwilson.launcher.ws.ext.V2;
import com.intel.mtwilson.core.common.tpm.model.IdentityProofRequest;
import com.intel.mtwilson.privacyca.v2.model.IdentityChallengeResponse;
import com.intel.mtwilson.privacyca.v2.rpc.IdentityRequestSubmitResponse;
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
@Path("/privacyca/identity-challenge-response")
public class IdentityRequestSubmitChallengeResponseResource {
    
    @POST
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public byte[] identityChallengeResponse(byte[] challengeResponse) throws Exception {
        throw new UnsupportedOperationException("Cannot use Octet Stream");
    }

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, DataMediaType.APPLICATION_YAML, DataMediaType.TEXT_YAML})
    public IdentityProofRequest identityChallengeResponse(IdentityChallengeResponse challengeResponse) throws Exception {
        IdentityRequestSubmitResponse rpc = new  IdentityRequestSubmitResponse();
        rpc.setIdentityRequest(challengeResponse.getIdentityRequest());
        rpc.setResponseToChallenge(challengeResponse.getResponseToChallenge());
        return rpc.call();
    }

}
