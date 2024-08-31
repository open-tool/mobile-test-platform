package com.atiurin.atp.kmpclient


data class FarmClientConfig(
    val farmUrls: List<FarmUrl>,
    var userAgent: String,
    var connectionTimeoutMs: Long = Const.CONNECTION_TIMEOUT,
    var readTimeoutMs: Long = Const.READ_TIMEOUT
){
    companion object {
        fun setNextFarmUrl(farmUrls: List<FarmUrl>){
            if (currentFarmUrl == null){
                currentFarmUrl = farmUrls.first()
            } else {
                val currentIndex = farmUrls.indexOf(currentFarmUrl)
                if (currentIndex == farmUrls.lastIndex){
                    currentFarmUrl = null
                    throw RuntimeException("No more available farm urls: $farmUrls")
                } else {
                    currentFarmUrl = farmUrls[currentIndex + 1]
                }
            }
        }
        fun getCurrentFarmUrl() = currentFarmUrl ?: throw RuntimeException("No farm url set")
        private var currentFarmUrl: FarmUrl? = null
    }
}
