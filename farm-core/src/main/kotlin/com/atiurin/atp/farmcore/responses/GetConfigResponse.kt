package com.atiurin.atp.farmcore.responses

import com.atiurin.atp.farmcore.models.Config

data class GetConfigResponse(val config: Config) :BaseResponse()