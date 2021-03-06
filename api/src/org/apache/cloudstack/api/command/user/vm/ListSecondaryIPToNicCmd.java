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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import org.apache.cloudstack.api.APICommand;
import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.ApiErrorCode;
import org.apache.cloudstack.api.BaseListCmd;
import org.apache.cloudstack.api.Parameter;
import org.apache.cloudstack.api.ServerApiException;
import org.apache.cloudstack.api.response.AddIpToVmNicResponse;
import org.apache.cloudstack.api.response.ListNicSecondaryIpResponse;
import org.apache.cloudstack.api.response.ListResponse;
import org.apache.cloudstack.api.response.NicResponse;

import com.cloud.async.AsyncJob;
import com.cloud.exception.ConcurrentOperationException;
import com.cloud.exception.InsufficientCapacityException;
import com.cloud.exception.ResourceAllocationException;
import com.cloud.exception.ResourceUnavailableException;
import com.cloud.user.Account;
import com.cloud.user.UserContext;
import com.cloud.vm.NicSecondaryIp;

@APICommand(name = "listNicIps", description = "Assigns secondary IP to NIC", responseObject = AddIpToVmNicResponse.class)
public class ListSecondaryIPToNicCmd extends BaseListCmd {
    public static final Logger s_logger = Logger.getLogger(ListSecondaryIPToNicCmd.class.getName());
    private static final String s_name = "listsecondaryipaddrtonicresponse";

    /////////////////////////////////////////////////////
    //////////////// API parameters /////////////////////
    /////////////////////////////////////////////////////

    @Parameter(name=ApiConstants.NIC_ID, type=CommandType.UUID, entityType = NicResponse.class, required = true,
        description="the ID of the nic to to list IPs")
    private Long nicId;

    @Parameter(name=ApiConstants.VIRTUAL_MACHINE_ID, type=CommandType.UUID, entityType = NicResponse.class, required = false,
        description="the ID of the vm")
    private Long vmId;
    
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

    public Long getNicId() {
        return nicId;
    }

    public Long getVmId() {
        return vmId;
    }

    @Override
    public long getEntityOwnerId() {
        Account caller = UserContext.current().getCaller();
        return caller.getAccountId();
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
 
        try {
            List<NicSecondaryIp> results = _networkService.listSecondaryIps(this);
            ListResponse<ListNicSecondaryIpResponse> response = new ListResponse<ListNicSecondaryIpResponse>();
            List<ListNicSecondaryIpResponse> resList = new ArrayList<ListNicSecondaryIpResponse>(results.size());
            for (NicSecondaryIp r : results) {
                ListNicSecondaryIpResponse resp = _responseGenerator.createListNicSecondaryIpResponse(r);
                resList.add(resp);
            }
            response.setResponses(resList);
            response.setResponseName(getCommandName());
            this.setResponseObject(response);

        } catch (Exception e) {
            s_logger.warn("Failed to list secondary ip address per nic ");
            throw new ServerApiException(ApiErrorCode.INTERNAL_ERROR, e.getMessage());
        }
    }

    @Override
    public AsyncJob.Type getInstanceType() {
        return AsyncJob.Type.IpAddress;
    }

}
