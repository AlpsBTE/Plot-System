package com.alpsbte.plotsystem.api.dto

import com.google.gson.annotations.SerializedName

data class FTPConfigurationDTO(var id: Int) {
    @SerializedName("id")
    var ftpConfigurationId: Int = id

    @SerializedName("schematics_path")
    var schematicsPath: String? = null

    @SerializedName("address")
    var address: String? = null

    @SerializedName("port")
    var port: Int? = null

    @SerializedName("is_sftp")
    var isSFTP: Boolean? = null

    @SerializedName("username")
    var username: String? = null

    @SerializedName("password")
    var password: String? = null
}