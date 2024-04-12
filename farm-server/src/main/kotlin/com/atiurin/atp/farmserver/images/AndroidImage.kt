package com.atiurin.atp.farmserver.images

object AndroidImage {
    private val images = mutableMapOf(
        "30" to "us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2"
    )

    fun set(imagesMap: Map<String, String>){
        imagesMap.forEach { (groupId, image) ->
            images[groupId] = image
        }
    }

    fun update(groupId: String, image: String){
        images[groupId] = image
    }

    fun get(groupId: String) = images[groupId] ?: throw RuntimeException("No image for group with id = '$groupId' configured")
}
