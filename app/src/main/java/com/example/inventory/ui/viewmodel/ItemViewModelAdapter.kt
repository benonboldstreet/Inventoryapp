package com.example.inventory.ui.viewmodel

import com.example.inventory.data.mapper.ItemMapper
import com.example.inventory.data.model.Item
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

/**
 * Adapter to help transition between Room database entities and Firestore models
 * This acts as a compatibility layer during the migration process
 */
object ItemViewModelAdapter {
    /**
     * Convert a model Item to a map for Firestore
     * (Replaces the old toDatabase method)
     */
    fun Item.toFirestore(): Map<String, Any?> {
        return ItemMapper.run { toMap() }
    }
    
    /**
     * Convert a list of database entities to model objects
     * This is a placeholder method that simply returns the input list
     * since we're now using model objects directly
     */
    fun List<Item>.toModelList(): List<Item> {
        return this
    }
    
    /**
     * Convert a Flow of database entities to a Flow of model objects
     */
    fun convertItemListFlow(flow: Flow<List<Item>>): Flow<List<Item>> {
        return flow.map { it.toModelList() }
    }
    
    /**
     * Convert a Flow of a single database entity to a Flow of a model object
     */
    fun convertSingleItemFlow(flow: Flow<Item?>): Flow<Item?> {
        return flow.map { it }
    }
} 