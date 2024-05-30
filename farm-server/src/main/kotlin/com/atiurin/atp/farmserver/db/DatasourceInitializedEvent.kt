package com.atiurin.atp.farmserver.db

import org.springframework.context.ApplicationEvent

class DatasourceInitializedEvent(source: Any) : ApplicationEvent(source)