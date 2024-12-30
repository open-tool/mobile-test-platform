package com.atiurin.atp.farmcore.api.response

import com.atiurin.atp.farmcore.entity.Config

data class GetConfigResponse(val config: Config) : BaseResponse()