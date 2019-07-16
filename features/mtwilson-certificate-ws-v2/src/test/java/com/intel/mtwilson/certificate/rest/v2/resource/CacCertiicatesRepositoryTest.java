/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */
package com.intel.mtwilson.certificate.rest.v2.resource;

import com.intel.mtwilson.certificate.rest.v2.model.CaCertificate;
import com.intel.mtwilson.certificate.rest.v2.model.CaCertificateCollection;
import com.intel.mtwilson.certificate.rest.v2.model.CaCertificateFilterCriteria;
import com.intel.mtwilson.certificate.rest.v2.repository.CaCertificateRepository;
import java.util.List;
import org.junit.Test;

/**
 *
 * @author jbuhacoff
 */
public class CacCertiicatesRepositoryTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CacCertiicatesRepositoryTest.class);

    @Test
    public void testSearchEkCertificates() {
        CaCertificateFilterCriteria criteria = new CaCertificateFilterCriteria();
        criteria.domain = "ek";
        CaCertificateRepository repository = new CaCertificateRepository();
        CaCertificateCollection searchResults = repository.search(criteria);
        List<CaCertificate> documents = searchResults.getCaCertificates();
        for(CaCertificate item : documents) {
            log.debug("search result: {}", item.getX509Certificate().getSubjectX500Principal().getName());
        }
    }
}
