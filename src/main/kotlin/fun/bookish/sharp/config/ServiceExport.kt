package `fun`.bookish.sharp.config

import `fun`.bookish.sharp.protocol.config.ProtocolConfig
import `fun`.bookish.sharp.registry.api.RegistryCenter
import `fun`.bookish.sharp.registry.api.RegistryCenterType
import `fun`.bookish.sharp.registry.config.RegistryConfig
import `fun`.bookish.sharp.registry.support.redis.RedisRegistryCenter
import `fun`.bookish.sharp.registry.support.zookeeper.ZkRegistryCenter
import `fun`.bookish.sharp.remoting.netty4.core.NettyServer

/**
 * 服务引用
 */
class ServiceExport<T> {

    val registryCenters:MutableList<RegistryCenter> = mutableListOf()

    val protocols:MutableList<ProtocolConfig> = mutableListOf()

    lateinit var serviceInterface:Class<T>

    lateinit var serviceRef:Any

    var version = "1.0.0"

    fun setRegistry(registry: RegistryConfig):ServiceExport<T>{
        val registryCenter = registry.registryCenter?:createRegistryCenter(registry)
        this.registryCenters.add(registryCenter)
        return this
    }

    fun setRegistries(registries: List<RegistryConfig>):ServiceExport<T>{
        for(registry in registries){
            val registryCenter = registry.registryCenter?:createRegistryCenter(registry)
            this.registryCenters.add(registryCenter)
        }
        return this
    }

    private fun createRegistryCenter(registryConfig: RegistryConfig):RegistryCenter{
        return when(registryConfig.type) {
            RegistryCenterType.REDIS -> {
                RedisRegistryCenter(registryConfig)
            }
            RegistryCenterType.ZOOKEEPER -> {
                ZkRegistryCenter(registryConfig)
            }
        }
    }

    fun setProtocol(protocol: ProtocolConfig):ServiceExport<T>{
        this.protocols.add(protocol)
        return this
    }

    fun setProtocols(protocols: List<ProtocolConfig>):ServiceExport<T>{
        for (protocol in protocols){
            this.protocols.add(protocol)
        }
        return this
    }

    fun setInterface(clazz:Class<T>):ServiceExport<T>{
        this.serviceInterface = clazz
        return this
    }

    fun setRef(ref:Any):ServiceExport<T>{
        this.serviceRef = ref
        return this
    }

    fun setVersion(version:String):ServiceExport<T>{
        this.version = version
        return this
    }

    /**
     * 注册并暴露服务
     */
    fun export(){
        NettyServer.start(this)
    }

}