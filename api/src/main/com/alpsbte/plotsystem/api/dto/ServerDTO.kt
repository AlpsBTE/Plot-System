package com.alpsbte.plotsystem.api.dto

import com.google.gson.annotations.SerializedName

class ServerDTO(var id: Int) {
    @SerializedName("id")
    var serverId: Int = id

    @SerializedName("ftp_configuration_id")
    var ftpConfigurationId: Int? = null

    @SerializedName("name")
    var name: String? = null
}