/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.tls.policy.codec.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intel.mtwilson.jaxrs2.provider.JacksonObjectMapperProvider;
import com.intel.mtwilson.tls.policy.TlsPolicyDescriptor;
import com.intel.mtwilson.tls.policy.codec.TlsPolicyReader;
import java.io.IOException;
import java.nio.charset.Charset;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author jbuhacoff
 */
public class JsonTlsPolicyReader implements TlsPolicyReader {

    @Override
    public boolean accept(String contentType) {
        MediaType mediaType = MediaType.valueOf(contentType);
        return mediaType.getType().equals("application") && mediaType.getSubtype().equals("json");
    }

    @Override
    public TlsPolicyDescriptor read(byte[] content) {
        try {
            ObjectMapper json = JacksonObjectMapperProvider.createDefaultMapper();
            TlsPolicyDescriptor tlsPolicyDescriptor = json.readValue(new String(content, Charset.forName("UTF-8")), TlsPolicyDescriptor.class);
            return tlsPolicyDescriptor;
        }
        catch(IOException e) {
            throw new IllegalArgumentException(e); // it's not TlsPolicyDescriptorInvalidException because we weren't able to read the content... there is no descriptor yet
        }
    }


}
