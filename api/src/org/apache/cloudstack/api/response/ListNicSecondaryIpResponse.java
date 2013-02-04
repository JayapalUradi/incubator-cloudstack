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
package org.apache.cloudstack.api.response;

import org.apache.cloudstack.api.ApiConstants;
import org.apache.cloudstack.api.BaseResponse;

import com.cloud.serializer.Param;
import com.google.gson.annotations.SerializedName;

public class ListNicSecondaryIpResponse extends BaseResponse {
    @SerializedName(ApiConstants.ID) @Param(description="the id of secondary ip address.")
    private Long id;

    @SerializedName(ApiConstants.NIC_ID) @Param(description="the ID of the NIC")
    private Long nicId;

    @SerializedName(ApiConstants.VIRTUAL_MACHINE_ID) @Param(description="the ID of the VM")
    private Long vmId;

    @SerializedName(ApiConstants.IP_ADDRESS) @Param(description="the IP address of the NIC")
    private String ipAddr;

    public void setid(Long id) {
        this.id = id;
    }

    public void setNicId(Long nicId) {
        this.nicId = nicId;
    }

    public void setIpAddr(String ip) {
        this.ipAddr = ip;
    }

    public void setVmId(Long vmId) {
        this.vmId = vmId;
    }
}
