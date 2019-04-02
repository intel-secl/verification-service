/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.flavor.runnable;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLink;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkCollection;
import com.intel.mtwilson.flavor.rest.v2.model.FlavorgroupHostLinkFilterCriteria;
import com.intel.mtwilson.flavor.rest.v2.repository.FlavorgroupHostLinkRepository;
import com.intel.mtwilson.flavor.rest.v2.repository.HostRepository;
import com.intel.mtwilson.flavor.rest.v2.resource.HostResource;
import static com.intel.mtwilson.i18n.HostState.QUEUE;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author hmgowda
 */
public class AddFlavorgroupHostsToFlavorVerifyQueue implements Runnable {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AddFlavorgroupHostsToFlavorVerifyQueue.class);
    private final UUID flavorgroupId;
    private final boolean forceUpdate;

    public AddFlavorgroupHostsToFlavorVerifyQueue(UUID flavorgroupId, boolean forceUpdate) {
        this.flavorgroupId = flavorgroupId;
        this.forceUpdate = forceUpdate;
    }

    @Override
    public void run() {
        addFlavorgroupHostsToFlavorVerifyQueue(flavorgroupId, forceUpdate);
    }
    
    private static synchronized void addFlavorgroupHostsToFlavorVerifyQueue(UUID flavorgroupId, boolean forceUpdate) {
        // retrieve the list of hosts associated the flavor group
        FlavorgroupHostLinkFilterCriteria flavorgroupHostLinkFilterCriteria
                = new FlavorgroupHostLinkFilterCriteria();
        flavorgroupHostLinkFilterCriteria.flavorgroupId = flavorgroupId;
        FlavorgroupHostLinkCollection flavorgroupHostLinkCollection
                = new FlavorgroupHostLinkRepository().search(flavorgroupHostLinkFilterCriteria);

        // Return if no hosts are associated to the flavorgroup
        if (flavorgroupHostLinkCollection == null || flavorgroupHostLinkCollection.getFlavorgroupHostLinks() == null
                || flavorgroupHostLinkCollection.getFlavorgroupHostLinks().isEmpty()) {
            return;
        }
        
        // Parse out the hostId list from flavorgroup host link association
        List<String> hostIdList = new ArrayList();
        for (FlavorgroupHostLink flavorgroupHostLink : flavorgroupHostLinkCollection.getFlavorgroupHostLinks()) {
            hostIdList.add(flavorgroupHostLink.getHostId().toString());
        }
        
        //Filter hosts already in the queue, update host statuses to queue and add the hosts to queue
        List<String> hostListForFlavorVerifyQueue = new HostRepository().filterHostsAlreadyInQueue(hostIdList, forceUpdate);
        if(hostListForFlavorVerifyQueue != null && !hostListForFlavorVerifyQueue.isEmpty()) {
            new HostResource().updateHostStatusList(hostListForFlavorVerifyQueue, QUEUE, null);
            new HostResource().addHostsToFlavorVerifyQueue(hostListForFlavorVerifyQueue, forceUpdate);
        }        
    }
}

