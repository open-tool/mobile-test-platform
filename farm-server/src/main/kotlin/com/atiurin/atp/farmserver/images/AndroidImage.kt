package com.atiurin.atp.farmserver.images

object AndroidImage {
    private val images = mutableMapOf(
        30 to "us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2"
    )
    fun set(imagesMap: Map<String, String>){
        imagesMap.forEach { (api, image) ->
            images[api.toInt()] = image
        }
    }

    fun get(api: Int) = images[api] ?: throw RuntimeException("No image for api $api configured")
}
