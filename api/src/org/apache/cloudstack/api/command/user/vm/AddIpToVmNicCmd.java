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
package org.apache.cloudstack.api.command.user.vm;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseAsyncCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AddIpToVmNicResponse;
import org.apache.cloudstack.api.response.NicResponse;
import com.cloud.async.AsyncJob;
import com.cloud.event.EventTypes;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientAddressCapacityException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.InvalidParameterValueException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.network.Network;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.utils.net.NetUtils;
import com.cloud.vm.Nic;

@APICommand(name = "addIpToNic", description = "Assigns secondary IP to NIC", responseObject = AddIpToVmNicResponse.class)
public class AddIpToVmNicCmd extends BaseAsyncCmd {
    public static final Logger s_logger = Logger.getLogger(AddIpToVmNicCmd.class.getName());
    private static final String s_name = "addiptovmnicresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////
    @Parameter(name=ApiConstants.NIC_ID, type=CommandType.UUID, entityType = NicResponse.class, required = true,
            description="the ID of the nic to which you want to assign private IP")
        private Long nicId;

    @Parameter(name = ApiConstants.IP_ADDRESS, type = CommandType.STRING, required = false, 
                description = "Secondary IP Address")
        private String ipAddr;

    /////////////////////////////////////////////////////
    /////////////////// Accessors ///////////////////////
    /////////////////////////////////////////////////////

    public String getEntityTable() {
    	return "nic_secondary_ips";
    }

    public String getAccountName() {
        return UserContext.current().getCaller().getAccountName();
    }

    public long getDomainId() {
        return UserContext.current().getCaller().getDomainId();
    }

    private long getZoneId() {
            Network ntwk = _entityMgr.findById(Network.class, getNetworkId());
            if (ntwk == null) {
                throw new InvalidParameterValueException("Can't find zone id for specified");
            }
            return ntwk.getDataCenterId();
    }

    public Long getNetworkId() {
        Nic nic = _entityMgr.findById(Nic.class, nicId);
        Long networkId = nic.getNetworkId();
        return networkId;
    }

    public Long getNicId() {
        return nicId;
    }

    public String getIpaddress () {
        if (ipAddr != null) {
            return ipAddr;
        } else {
            return null;
        }
    }
    @Override
    public long getEntityOwnerId() {
        Account caller = UserContext.current().getCaller();
        return caller.getAccountId();
    }

    @Override
    public String getEventType() {
        return EventTypes.EVENT_NET_IP_ASSIGN;
    }
    
    @Override
    public String getEventDescription() {
        return  "associating ip to nic id: " + getNetworkId() + " in zone " + getZoneId();
    }

    /////////////////////////////////////////////////////
    /////////////// API Implementation///////////////////
    /////////////////////////////////////////////////////


    @Override
    public String getCommandName() {
        return s_name;
    }

    public static String getResultObjectName() {
    	return "addressinfo";
    }

    @Override
    public void execute() throws ResourceUnavailableException, ResourceAllocationException, 
                                    ConcurrentOperationException, InsufficientCapacityException {

        UserContext.current().setEventDetails("Nic Id: " + getNicId() );
        String ip;
        String SecondaryIp = null;
        if ((ip = getIpaddress()) != null) {
            if (!NetUtils.isValidIp(ip)) {
                throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Invalid ip address " + ip); 
            }
        }

        try {
            SecondaryIp =  _networkService.allocateSecondaryGuestIP(_accountService.getAccount(getEntityOwnerId()),  getZoneId(), getNicId(), getNetworkId(), getIpaddress());
        } catch (InsufficientAddressCapacityException e) {
            throw new InvalidParameterValueException("Allocating guest ip for nic failed");
        }

        if (SecondaryIp != null) {
            s_logger.info("Associated ip address to NIC : " + SecondaryIp);
            AddIpToVmNicResponse response = new AddIpToVmNicResponse();
            response = _responseGenerator.createSecondaryIPToNicResponse(ip, getNicId(), getNetworkId());
            response.setResponseName(getCommandName());
            this.setResponseObject(response);
        } else {
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, "Failed to assign secondary ip to nic");
        }
    }

    @Override
    public String getSyncObjType() {
        return BaseAsyncCmd.networkSyncObject;
    }

    @Override
    public Long getSyncObjId() {
        return getNetworkId();
    }
    
    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.IpAddress;
    }

}