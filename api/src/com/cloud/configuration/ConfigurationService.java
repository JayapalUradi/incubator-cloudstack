// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.configuration;

import java.util.List;

import javax.naming.NamingException;

import org.apache.cloudstack.api.command.admin.config.UpdateCfgCmd;
import org.apache.cloudstack.api.command.admin.ldap.LDAPConfigCmd;
import org.apache.cloudstack.api.command.admin.ldap.LDAPRemoveCmd;
import org.apache.cloudstack.api.command.admin.network.CreateNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.network.DeleteNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.network.UpdateNetworkOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.CreateDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.CreateServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.DeleteDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.DeleteServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.UpdateDiskOfferingCmd;
import org.apache.cloudstack.api.command.admin.offering.UpdateServiceOfferingCmd;
import org.apache.cloudstack.api.command.admin.pod.DeletePodCmd;
import org.apache.cloudstack.api.command.admin.pod.UpdatePodCmd;
import org.apache.cloudstack.api.command.admin.vlan.CreateVlanIpRangeCmd;
import org.apache.cloudstack.api.command.admin.vlan.DeleteVlanIpRangeCmd;
import org.apache.cloudstack.api.command.admin.zone.CreateZoneCmd;
import org.apache.cloudstack.api.command.admin.zone.DeleteZoneCmd;
import org.apache.cloudstack.api.command.admin.zone.UpdateZoneCmd;
import org.apache.cloudstack.api.command.user.network.ListNetworkOfferingsCmd;

import com.cloud.dc.DataCenter;
import com.cloud.dc.Pod;
import com.cloud.dc.Vlan;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Networks.TrafficType;
import com.cloud.offering.DiskOffering;
import com.cloud.offering.NetworkOffering;
import com.cloud.offering.ServiceOffering;
import com.cloud.user.Account;

public interface ConfigurationService {

    /**
     * Updates a configuration entry with a new value
     *
     * @param cmd
     *            - the command wrapping name and value parameters
     * @return updated configuration object if successful
     */
    Configuration updateConfiguration(UpdateCfgCmd cmd);

    /**
     * Create a service offering through the API
     *
     * @param cmd
     *            the command object that specifies the name, number of cpu cores, amount of RAM, etc. for the service
     *            offering
     * @return the newly created service offering if successful, null otherwise
     */
    ServiceOffering createServiceOffering(CreateServiceOfferingCmd cmd);

    /**
     * Updates a service offering
     *
     * @param serviceOfferingId
     * @param userId
     * @param name
     * @param displayText
     * @param offerHA
     * @param useVirtualNetwork
     * @param tags
     * @return updated service offering
     */
    ServiceOffering updateServiceOffering(UpdateServiceOfferingCmd cmd);

    /**
     * Deletes a service offering
     *
     * @param userId
     * @param serviceOfferingId
     */
    boolean deleteServiceOffering(DeleteServiceOfferingCmd cmd);

    /**
     * Updates a disk offering
     *
     * @param cmd
     *            - the command specifying diskOfferingId, name, description, tags
     * @return updated disk offering
     * @throws
     */
    DiskOffering updateDiskOffering(UpdateDiskOfferingCmd cmd);

    /**
     * Deletes a disk offering
     *
     * @param cmd
     *            - the command specifying disk offering id
     * @return true or false
     * @throws
     */
    boolean deleteDiskOffering(DeleteDiskOfferingCmd cmd);

    /**
     * Creates a new disk offering
     *
     * @param domainId
     * @param name
     * @param description
     * @param numGibibytes
     * @param mirrored
     * @param size
     * @return ID
     */
    DiskOffering createDiskOffering(CreateDiskOfferingCmd cmd);

    /**
     * Creates a new pod based on the parameters specified in the command object
     *
     * @param zoneId
     *            TODO
     * @param name
     *            TODO
     * @param startIp
     *            TODO
     * @param endIp
     *            TODO
     * @param gateway
     *            TODO
     * @param netmask
     *            TODO
     * @param allocationState
     *            TODO
     * @return the new pod if successful, null otherwise
     * @throws
     * @throws
     */
    Pod createPod(long zoneId, String name, String startIp, String endIp, String gateway, String netmask, String allocationState);

    /**
     * Edits a pod in the database. Will not allow you to edit pods that are being used anywhere in the system.
     *
     * @param UpdatePodCmd
     *            api command
     */
    Pod editPod(UpdatePodCmd cmd);

    /**
     * Deletes a pod from the database. Will not allow you to delete pods that are being used anywhere in the system.
     *
     * @param cmd
     *            - the command containing podId
     * @return true or false
     * @throws ,
     */
    boolean deletePod(DeletePodCmd cmd);

    /**
     * Creates a new zone
     *
     * @param cmd
     * @return the zone if successful, null otherwise
     * @throws
     * @throws
     */
    DataCenter createZone(CreateZoneCmd cmd);

    /**
     * Edits a zone in the database. Will not allow you to edit DNS values if there are VMs in the specified zone.
     *
     * @param UpdateZoneCmd
     * @return Updated zone
     */
    DataCenter editZone(UpdateZoneCmd cmd);

    /**
     * Deletes a zone from the database. Will not allow you to delete zones that are being used anywhere in the system.
     *
     * @param userId
     * @param zoneId
     */
    boolean deleteZone(DeleteZoneCmd cmd);

    /**
     * Adds a VLAN to the database, along with an IP address range. Can add three types of VLANs: (1) zone-wide VLANs on
     * the
     * virtual public network (2) pod-wide direct attached VLANs (3) account-specific direct attached VLANs
     *
     * @param userId
     * @param vlanType
     *            - either "DomR" (VLAN for a virtual public network) or "DirectAttached" (VLAN for IPs that will be
     *            directly
     *            attached to UserVMs)
     * @param zoneId
     * @param accountId
     * @param podId
     * @param add
     * @param vlanId
     * @param gateway
     * @param startIP
     * @param endIP
     * @throws ResourceAllocationException TODO
     * @throws
     * @return The new Vlan object
     */
    Vlan createVlanAndPublicIpRange(CreateVlanIpRangeCmd cmd) throws InsufficientCapacityException, ConcurrentOperationException, ResourceUnavailableException, ResourceAllocationException;

    /**
     * Marks the the account with the default zone-id.
     *
     * @param accountName
     * @param domainId
     * @param zoneId
     * @return The new account object
     * @throws ,
     */
    Account markDefaultZone(String accountName, long domainId, long defaultZoneId);

    boolean deleteVlanIpRange(DeleteVlanIpRangeCmd cmd);

    NetworkOffering createNetworkOffering(CreateNetworkOfferingCmd cmd);

    NetworkOffering updateNetworkOffering(UpdateNetworkOfferingCmd cmd);

    List<? extends NetworkOffering> searchForNetworkOfferings(ListNetworkOfferingsCmd cmd);

    boolean deleteNetworkOffering(DeleteNetworkOfferingCmd cmd);

    NetworkOffering getNetworkOffering(long id);

    Integer getNetworkOfferingNetworkRate(long networkOfferingId);

    Account getVlanAccount(long vlanId);

    List<? extends NetworkOffering> listNetworkOfferings(TrafficType trafficType, boolean systemOnly);

    DataCenter getZone(long id);

    ServiceOffering getServiceOffering(long serviceOfferingId);

    Long getDefaultPageSize();

    Integer getServiceOfferingNetworkRate(long serviceOfferingId);

    DiskOffering getDiskOffering(long diskOfferingId);

    boolean updateLDAP(LDAPConfigCmd cmd) throws NamingException;

	boolean removeLDAP(LDAPRemoveCmd cmd);

    /**
     * @param offering
     * @return
     */
    boolean isOfferingForVpc(NetworkOffering offering);
}
