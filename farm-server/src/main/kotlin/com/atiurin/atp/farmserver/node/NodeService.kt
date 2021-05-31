package com.atiurin.atp.farmserver.node

import org.springframework.stereotype.Service

@Service
class NodeService(val repository: NodeRepository)  {
    fun create(node: Node){
        repository.save(node)
    }

    fun delete(id: String){
        repository.deleteById(id)
    }

    fun delete(node: Node){
        repository.delete(node)
    }

    fun all(): MutableIterable<Node> = repository.findAll()
}