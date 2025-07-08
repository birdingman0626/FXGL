/*
 * FXGL - JavaFX Game Library. The MIT License (MIT).
 * Copyright (c) AlmasB (almaslvl@gmail.com).
 * See LICENSE for details.
 */

package com.almasb.fxgl.multiplayer

import com.almasb.fxgl.core.EngineService
import com.almasb.fxgl.core.collection.MovingAverageQueue
import com.almasb.fxgl.core.collection.PropertyMap
import com.almasb.fxgl.core.collection.PropertyMapChangeListener
import com.almasb.fxgl.core.serialization.Bundle
import com.almasb.fxgl.entity.Entity
import com.almasb.fxgl.entity.GameWorld
import com.almasb.fxgl.entity.SpawnData
import com.almasb.fxgl.event.EventBus
import com.almasb.fxgl.input.*
import com.almasb.fxgl.logging.Logger
import com.almasb.fxgl.net.Connection
import javafx.beans.property.ReadOnlyDoubleProperty
import javafx.beans.property.ReadOnlyDoubleWrapper

/**
 * Manages multiplayer connections and data replication across network.
 * Provides functionality for entity, input, property, and event replication.
 *
 * @author Almas Baimagambetov (almaslvl@gmail.com)
 */
class MultiplayerService : EngineService() {

    private val log = Logger.get(javaClass)

    private val replicatedEntitiesMap = hashMapOf<Connection<Bundle>, ConnectionData>()

    fun registerConnection(connection: Connection<Bundle>) {
        val data = ConnectionData(connection)
        setUpNewConnection(data)

        replicatedEntitiesMap[connection] = data
    }

    /**
     * Removes a connection and cleans up all associated data.
     */
    fun unregisterConnection(connection: Connection<Bundle>) {
        val data = replicatedEntitiesMap.remove(connection)
        if (data != null) {
            cleanUpConnection(data)
            log.debug("Connection unregistered and cleaned up")
        } else {
            log.warning("Attempted to unregister unknown connection")
        }
    }

    private fun cleanUpConnection(data: ConnectionData) {
        val connection = data.connection
        
        // Clean up all entities associated with this connection
        data.entities.clear()
        
        // Clean up all replication handlers
        inputReplicationSenders.remove(connection)
        propertyReplicationSenders.remove(connection)
        inputReplicationReceivers.remove(connection)
        propertyReplicationReceivers.remove(connection)
        eventReplicationSenders.remove(connection)
        eventReplicationReceivers.remove(connection)
        entityReplicationReceivers.remove(connection)
        
        // Clear event bus handlers (EventBus handles this automatically when it's GC'd)
        // But we can explicitly help by clearing any references
        data.eventBus.removeAllEventHandlers()
    }

    private fun setUpNewConnection(data: ConnectionData) {
        // register event handler for the given connection
        addEventReplicationReceiver(data.connection, data.eventBus)
        
        // Monitor connection state and clean up when it dies
        data.connection.connectedProperty().addListener { _, _, isConnected ->
            if (!isConnected) {
                log.debug("Connection closed, cleaning up")
                unregisterConnection(data.connection)
            }
        }

        data.eventBus.addEventHandler(ReplicationEvent.PING) { ping ->
            val timeRecv = System.nanoTime()
            fire(data.connection, PongReplicationEvent(ping.timeSent, timeRecv))
        }

        data.eventBus.addEventHandler(ReplicationEvent.PONG) { pong ->
            val timeNow = System.nanoTime()
            val roundTripTime = timeNow - pong.timeSent

            data.pingBuffer.put(roundTripTime.toDouble())
            data.ping.value = data.pingBuffer.average
        }
    }

    override fun onGameUpdate(tpf: Double) {
        if (replicatedEntitiesMap.isEmpty())
            return

        val now = System.nanoTime()

        replicatedEntitiesMap.forEach { conn, data ->
            // Check if connection is still alive before sending data
            if (conn.isConnected) {
                fire(conn, PingReplicationEvent(now))

                if (data.entities.isNotEmpty()) {
                    updateReplicatedEntities(conn, data.entities)
                }
            } else {
                // Connection died, schedule for cleanup
                log.debug("Detected dead connection during update, scheduling cleanup")
                unregisterConnection(conn)
            }
        }
    }

    /**
     * @return round-trip time from this endpoint to given [connection], or null if connection not found
     */
    fun pingProperty(connection: Connection<Bundle>): ReadOnlyDoubleProperty? {
        val data = replicatedEntitiesMap[connection]
        if (data == null) {
            log.warning("Attempted to get ping for unknown connection")
            return null
        }
        return data.ping.readOnlyProperty
    }

    private fun updateReplicatedEntities(connection: Connection<Bundle>, entities: MutableList<Entity>) {
        val events = arrayListOf<ReplicationEvent>()

        entities.forEach {
            val networkComponent = it.getComponentOptional(NetworkComponent::class.java)
            if (!networkComponent.isPresent) {
                log.warning("Entity missing NetworkComponent during replication update")
                return@forEach
            }
            val networkID = networkComponent.get().id

            if (it.isActive) {
                events += EntityUpdateEvent(networkID, it.x, it.y, it.z)
            } else {
                events += EntityRemoveEvent(networkID)
            }
        }

        if (events.isNotEmpty()) {
            fire(connection, *events.toTypedArray())
        }

        entities.removeIf { !it.isActive }
    }

    fun spawn(connection: Connection<Bundle>, entity: Entity, entityName: String) {
        spawn(connection, entity, entityName, SpawnData(entity.x, entity.y, entity.z))
    }

    fun spawn(connection: Connection<Bundle>, entity: Entity, entityName: String, spawnData: SpawnData) {
        if (!entity.hasComponent(NetworkComponent::class.java)) {
            log.warning("Attempted to network-spawn entity $entityName, but it does not have NetworkComponent")
            return
        }

        val networkComponent = entity.getComponent(NetworkComponent::class.java)

        val event = EntitySpawnEvent(networkComponent.id, entityName, NetworkSpawnData(spawnData))

        val data = replicatedEntitiesMap[connection]
        if (data == null) {
            log.warning("Attempted to spawn entity on unknown connection")
            return
        }
        data.entities += entity

        fire(connection, event)
    }

    fun addEntityReplicationReceiver(connection: Connection<Bundle>, gameWorld: GameWorld) {
        connection.addMessageHandlerFX { _, message ->

            handleIfReplicationBundle(message) { event ->
                when (event) {
                    is EntitySpawnEvent -> {
                        val id = event.networkID
                        val entityName = event.entityName

                        val networkSpawnData = event.networkSpawnData
                        val data = networkSpawnData.bundle.data
                        val spawnData = SpawnData(networkSpawnData.x, networkSpawnData.y, networkSpawnData.z)
                        data.forEach(spawnData::put)

                        val e = gameWorld.spawn(entityName, spawnData)

                        val networkComponent = e.getComponentOptional(NetworkComponent::class.java)
                        if (networkComponent.isPresent) {
                            networkComponent.get().id = id
                        } else {
                            log.warning("Spawned entity $entityName does not have NetworkComponent")
                        }
                    }

                    is EntityUpdateEvent -> {
                        val id = event.networkID

                        gameWorld.getEntitiesByComponentMapped(NetworkComponent::class.java)
                                .filterValues { it.id == id }
                                .forEach { (e, _) -> e.setPosition3D(event.x, event.y, event.z) }
                    }

                    is EntityRemoveEvent -> {
                        val id = event.networkID

                        gameWorld.getEntitiesByComponentMapped(NetworkComponent::class.java)
                                .filterValues { it.id == id }
                                .forEach { (e, _) -> e.removeFromWorld() }
                    }
                }
            }
        }
    }

    private val inputReplicationSenders = hashMapOf<Connection<Bundle>, TriggerListener>()
    private val propertyReplicationSenders = hashMapOf<Connection<Bundle>, PropertyMapChangeListener>()
    private val inputReplicationReceivers = hashMapOf<Connection<Bundle>, (Connection<Bundle>, Bundle) -> Unit>()
    private val propertyReplicationReceivers = hashMapOf<Connection<Bundle>, (Connection<Bundle>, Bundle) -> Unit>()
    private val eventReplicationSenders = hashMapOf<Connection<Bundle>, (ReplicationEvent) -> Unit>()
    private val eventReplicationReceivers = hashMapOf<Connection<Bundle>, (Connection<Bundle>, Bundle) -> Unit>()
    private val entityReplicationReceivers = hashMapOf<Connection<Bundle>, (Connection<Bundle>, Bundle) -> Unit>()

    fun addInputReplicationSender(connection: Connection<Bundle>, input: Input) {
        val listener = object : TriggerListener() {
            override fun onActionBegin(trigger: Trigger) {

                val event = if (trigger.isKey) {
                    ActionBeginReplicationEvent(key = ((trigger) as KeyTrigger).key)
                } else {
                    ActionBeginReplicationEvent(btn = ((trigger) as MouseTrigger).button)
                }

                fire(connection, event)
            }

            override fun onActionEnd(trigger: Trigger) {
                val event = if (trigger.isKey) {
                    ActionEndReplicationEvent(key = ((trigger) as KeyTrigger).key)
                } else {
                    ActionEndReplicationEvent(btn = ((trigger) as MouseTrigger).button)
                }

                fire(connection, event)
            }
        }
        
        input.addTriggerListener(listener)
        inputReplicationSenders[connection] = listener
    }

    fun removeInputReplicationSender(connection: Connection<Bundle>, input: Input) {
        val listener = inputReplicationSenders.remove(connection)
        if (listener != null) {
            input.removeTriggerListener(listener)
            log.debug("Removed input replication sender for connection")
        } else {
            log.warning("Attempted to remove unknown input replication sender")
        }
    }

    fun addPropertyReplicationSender(connection: Connection<Bundle>, map: PropertyMap) {
        map.addListener(object : PropertyMapChangeListener {
            override fun onUpdated(propertyName: String, propertyValue: Any) {
                val event = PropertyUpdateReplicationEvent(propertyName, propertyValue)

                fire(connection, event)
            }

            override fun onRemoved(propertyName: String, propertyValue: Any) {
                val event = PropertyRemoveReplicationEvent(propertyName)

                fire(connection, event)
            }
        })
    }

    fun addPropertyReplicationReceiver(connection: Connection<Bundle>, map: PropertyMap) {
        connection.addMessageHandlerFX { _, message ->
            handleIfReplicationBundle(message) { event ->

                when (event) {
                    is PropertyUpdateReplicationEvent -> {
                        map.setValue(event.propertyName, event.propertyValue)
                    }

                    is PropertyRemoveReplicationEvent -> {
                        map.remove(event.propertyName)
                    }
                }
            }
        }
    }

    fun addInputReplicationReceiver(connection: Connection<Bundle>, input: Input) {
        connection.addMessageHandlerFX { _, message ->

            handleIfReplicationBundle(message) { event ->

                if (event is ActionBeginReplicationEvent) {
                    event.key?.let {
                        input.mockTriggerPress(KeyTrigger(it))
                    }

                    event.btn?.let {
                        input.mockTriggerPress(MouseTrigger(it))
                    }

                } else if (event is ActionEndReplicationEvent) {
                    event.key?.let {
                        input.mockTriggerRelease(KeyTrigger(it))
                    }

                    event.btn?.let {
                        input.mockTriggerRelease(MouseTrigger(it))
                    }
                }
            }
        }
    }

    fun addEventReplicationSender(connection: Connection<Bundle>, eventBus: EventBus) {
        eventBus.addEventHandler(ReplicationEvent.ANY) { event ->
            fire(connection, event)
        }
    }

    fun addEventReplicationReceiver(connection: Connection<Bundle>, eventBus: EventBus) {
        connection.addMessageHandlerFX { _, message ->
            handleIfReplicationBundle(message) {
                eventBus.fireEvent(it)
            }
        }
    }

    private fun fire(connection: Connection<Bundle>, vararg events: ReplicationEvent) {
        if (!connection.isConnected)
            return

        // this is the only place where we create a replication event carrying bundle
        val bundle = Bundle("REPLICATION_EVENT")

        val list = ArrayList<ReplicationEvent>(events.toList())

        bundle.put("events", list)

        connection.send(bundle)
    }

    private fun handleIfReplicationBundle(bundle: Bundle, handler: (ReplicationEvent) -> Unit) {
        if (bundle.name == "REPLICATION_EVENT") {
            val events: List<ReplicationEvent> = bundle.get("events")

            events.forEach(handler)
        }
    }

    private class ConnectionData(val connection: Connection<Bundle>) {
        val entities = ArrayList<Entity>()
        val eventBus = EventBus().also { it.isLoggingEnabled = false }

        val pingBuffer = MovingAverageQueue(1000)
        val ping = ReadOnlyDoubleWrapper()
    }
}