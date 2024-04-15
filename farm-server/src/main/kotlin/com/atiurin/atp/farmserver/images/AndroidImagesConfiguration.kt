package com.atiurin.atp.farmserver.images

import com.atiurin.atp.farmserver.config.InitialArguments
import com.atiurin.atp.farmserver.logging.log
import org.springframework.stereotype.Component
import javax.inject.Singleton

@Singleton
@Component
class AndroidImagesConfiguration {
    private val images: MutableMap<String, String> = InitialArguments.config.imagesMap.ifEmpty {
        mutableMapOf(
            "30" to "us-docker.pkg.dev/android-emulator-268719/images/30-google-x64:30.1.2"
        )
    }.toMutableMap()

    fun set(imagesMap: Map<String, String>){
        imagesMap.forEach { (groupId, image) ->
            images[groupId] = image 
        }
    }

    fun update(groupId: String, image: String){
        images[groupId] = image
        log.info { "Android images updated: $images" }
    }

    fun get(groupId: String) = images[groupId] ?: throw RuntimeException("No image for group with id = '$groupId' configured")
}
