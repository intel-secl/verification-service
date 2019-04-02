/*
 * Copyright (C) 2019 Intel Corporation
 * SPDX-License-Identifier: BSD-3-Clause
 */

package com.intel.mtwilson.features.queue.repository;

import com.intel.dcsg.cpg.io.UUID;
import com.intel.mtwilson.My;
import com.intel.mtwilson.features.queue.model.Queue;
import com.intel.mtwilson.features.queue.model.QueueCollection;
import com.intel.mtwilson.features.queue.model.QueueFilterCriteria;
import com.intel.mtwilson.features.queue.model.QueueLocator;
import com.intel.mtwilson.features.queue.model.QueueState;
import static com.intel.mtwilson.features.queue.model.QueueState.NEW;
import com.intel.mtwilson.flavor.controller.MwQueueJpaController;
import com.intel.mtwilson.flavor.controller.exceptions.NonexistentEntityException;
import com.intel.mtwilson.flavor.data.MwQueue;
import com.intel.mtwilson.repository.RepositoryCreateException;
import com.intel.mtwilson.repository.RepositoryDeleteException;
import com.intel.mtwilson.repository.RepositoryException;
import com.intel.mtwilson.repository.RepositoryInvalidInputException;
import com.intel.mtwilson.repository.RepositoryRetrieveException;
import com.intel.mtwilson.repository.RepositorySearchException;
import com.intel.mtwilson.repository.RepositoryStoreException;
import java.io.IOException;
import java.util.List;
import org.apache.shiro.authz.annotation.RequiresPermissions;

/**
 *
 * @author hmgowda
 */
public class QueueRepository {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(QueueRepository.class);

    @RequiresPermissions("queue:search")
    public QueueCollection search(QueueFilterCriteria criteria) {
        log.debug("Received request to search the queue");
        QueueCollection queueCollection = new QueueCollection();
        try {
            MwQueueJpaController mwQueueJpaController = My.jpa().mwQueue();
            if (criteria.filter == false) {
                List<MwQueue> mwQueueList = mwQueueJpaController.findMwQueueEntities();
                if (mwQueueList != null && !mwQueueList.isEmpty()) {
                    for (MwQueue mwQueue : mwQueueList) {
                        queueCollection.getQueueEntries().add(convert(mwQueue));
                    }
                }
            } else if (criteria.id != null) {
                MwQueue mwQueue = mwQueueJpaController.findMwQueue(criteria.id.toString());
                if (mwQueue != null) {
                    queueCollection.getQueueEntries().add(convert(mwQueue));
                }
            } else if (criteria.action != null && !criteria.action.isEmpty()
                    && criteria.parameter != null && !criteria.parameter.isEmpty()
                    && criteria.value != null && !criteria.value.isEmpty()) {
                List<MwQueue> mwQueueList = mwQueueJpaController.findMwQueueByActionParameter(criteria.action, criteria.parameter, criteria.value);
                if (mwQueueList != null && !mwQueueList.isEmpty()) {
                    for (MwQueue mwQueue : mwQueueList) {
                        queueCollection.getQueueEntries().add(convert(mwQueue));
                    }
                }
            } else if (criteria.queueStates != null && !criteria.queueStates.isEmpty()) {
                List<MwQueue> mwQueueList = mwQueueJpaController.findMwQueueByQueueStates(criteria.queueStates);
                if (mwQueueList != null && !mwQueueList.isEmpty()) {
                    for (MwQueue mwQueue : mwQueueList) {
                        queueCollection.getQueueEntries().add(convert(mwQueue));
                    }
                }
            } else {
                // Invalid search criteria specified
                log.error("Invalid search criteria specified");
            }
        } catch (Exception ex) {
            log.error("Error during search of queue", ex);
            throw new RepositorySearchException(ex, criteria);
        }
        log.debug("Returning back {} results", queueCollection.getQueueEntries().size());
        return queueCollection;
    }

    @RequiresPermissions("queue:retrieve")
    public Queue retrieve(QueueLocator locator) {
        if (locator == null || locator.id == null) {
            return null;
        }
        log.debug("Received request to retrieve queue entry with ID {}", locator.id);
        try {
            MwQueueJpaController mwQueueJpaController = My.jpa().mwQueue();
            MwQueue mwQueue = mwQueueJpaController.findMwQueue(locator.id.toString());
            if (mwQueue != null) {
                return convert(mwQueue);
            }
        } catch (Exception ex) {
            log.error("Error during queue entry retrieval", ex);
            throw new RepositoryRetrieveException(ex, locator);
        }
        return null;
    }
    
    public Queue store(Queue item) {
        log.debug("Received request to update queue entry");
        if (item == null || item.getId() == null) {
            log.error("Queue entry ID must be specified");
            throw new RepositoryInvalidInputException();
        }
        
        QueueLocator locator = new QueueLocator();
        locator.id = item.getId();
        
        try {
            MwQueueJpaController queueJpaController = My.jpa().mwQueue();
            MwQueue mwQueue = queueJpaController.findMwQueue(locator.id.toString());
            if (mwQueue == null) {
                log.error("Queue entry does not exist");
                throw new RepositoryInvalidInputException(locator);
            }
            
            if (item.getQueueAction() != null && !item.getQueueAction().isEmpty())
                mwQueue.setQueueAction(item.getQueueAction());
            if (item.getActionParameters() != null && !item.getActionParameters().isEmpty())
                mwQueue.setActionParameters(item.getActionParameters());
            if (item.getStatus() != null)
                mwQueue.setStatus(item.getStatus().name());
            if (item.getMessage() != null && !item.getMessage().isEmpty())
                mwQueue.setMessage(item.getMessage());
            
            queueJpaController.edit(mwQueue);
            log.debug("Updated the queue entry with id {} successfully", item.getId().toString());
            return retrieve(locator);
        } catch (RepositoryException re) {
            throw re;
        } catch (Exception ex) {
            log.error("Error during queue entry update", ex);
            throw new RepositoryStoreException(ex, locator);
        }
    }
    
    @RequiresPermissions("queue:create")
    public Queue create(Queue item) {
        log.debug("Received request to create a queue entry");
        UUID queueId;
        if (item.getId() != null) {
            queueId = item.getId();
        } else {
            queueId = new UUID();
        }
        
        QueueLocator locator = new QueueLocator();
        locator.id = queueId;
        
        if (item == null || item.getQueueAction() == null) {
            log.error("Queue action must be specified");
            throw new RepositoryInvalidInputException(locator);
        }
        
        try {
            MwQueue mwQueue = new MwQueue();
            mwQueue.setId(queueId.toString());
            mwQueue.setQueueAction(item.getQueueAction());
            mwQueue.setActionParameters(item.getActionParameters());
            mwQueue.setStatus(NEW.name());
            
            String hostId = item.getActionParameters().get("host_id");
            log.debug("Adding the host {} to flavor_verify queue", hostId);
            
            MwQueueJpaController mwQueueJpaController = My.jpa().mwQueue();
            mwQueueJpaController.create(mwQueue);
            
            return item;
        } catch (IOException ex) {
            log.error("Error during queue entry creation", ex);
            throw new RepositoryCreateException();
        } catch (Exception Ex) {
            log.error("Error during queue entry creation", Ex);
            throw new RepositoryCreateException();
        }
    }
    
    @RequiresPermissions("queue:delete")
    public void delete(QueueLocator locator) {
        log.debug("Received request to delete a queue entry");
        if (locator == null || locator.id == null) {
            return;
        }
        
        Queue queueEntry = retrieve(locator);
        if (queueEntry != null) {
            try {
                My.jpa().mwQueue().destroy(queueEntry.getId().toString());
            } catch (IOException | NonexistentEntityException ex) {
                log.error("Error during queue entry deletion", ex);
                throw new RepositoryDeleteException(ex);
            }
        }
    }
    
    @RequiresPermissions("queue:delete")
    public Queue delete(QueueFilterCriteria criteria) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    private Queue convert(MwQueue mwQueue) {
        Queue queue = new Queue();
        if (mwQueue != null) {
            queue.setId(UUID.valueOf(mwQueue.getId()));
            queue.setQueueAction(mwQueue.getQueueAction());
            queue.setActionParameters(mwQueue.getActionParameters());
            queue.setCreated(mwQueue.getCreated());
            queue.setUpdated(mwQueue.getUpdated());
            queue.setStatus(QueueState.valueOf(mwQueue.getStatus()));
            queue.setMessage(mwQueue.getMessage());
        }
        return queue;
    }
    
    public MwQueue convertToMwQueue(Queue queue) {
        MwQueue mwQueue = new MwQueue();
        if (queue != null) {
            if (queue.getId() != null) {
                mwQueue.setId(queue.getId().toString());
            }
            if (queue.getActionParameters() != null) {
                mwQueue.setActionParameters(queue.getActionParameters());
            }
            if (queue.getQueueAction() != null) {
                mwQueue.setQueueAction(queue.getQueueAction());
            }
            if (queue.getStatus() != null) {
                mwQueue.setStatus(String.valueOf(queue.getStatus()));
            }
            if (queue.getCreated() != null) {
                mwQueue.setCreated(queue.getCreated());
            }
            if (queue.getUpdated() != null) {
                mwQueue.setUpdated(queue.getUpdated());
            }
            if (queue.getMessage() != null) {
                mwQueue.setMessage(queue.getMessage());
            }
        }
        return mwQueue;
    }
}
