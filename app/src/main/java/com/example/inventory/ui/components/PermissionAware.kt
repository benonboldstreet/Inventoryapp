package com.example.inventory.ui.components

import androidx.compose.runtime.Composable
import com.example.inventory.api.RoleBasedAccessControl
import com.example.inventory.api.RoleBasedAccessControl.Permission

/**
 * PermissionAware component that only renders content if the user has the required permission(s)
 * 
 * @param permission Single permission required to view the content
 * @param permissions Multiple permissions, all of which are required to view the content
 * @param anyPermission Multiple permissions, any of which is sufficient to view the content
 * @param fallback Optional content to show when permission is denied
 * @param content Content to show when permission is granted
 */
@Composable
fun PermissionAware(
    permission: Permission? = null,
    permissions: List<Permission> = emptyList(),
    anyPermission: List<Permission> = emptyList(),
    fallback: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val hasPermission = when {
        // Single permission check
        permission != null -> 
            RoleBasedAccessControl.hasPermission(permission)
        
        // Multiple permissions - require all
        permissions.isNotEmpty() -> 
            RoleBasedAccessControl.hasAllPermissions(*permissions.toTypedArray())
        
        // Multiple permissions - any is sufficient
        anyPermission.isNotEmpty() -> 
            RoleBasedAccessControl.hasAnyPermission(*anyPermission.toTypedArray())
        
        // No permission specified - show content
        else -> true
    }
    
    if (hasPermission) {
        content()
    } else {
        fallback?.invoke()
    }
}

/**
 * PermissionButton that is only enabled if the user has the required permission(s)
 */
@Composable
fun PermissionButton(
    onClick: () -> Unit,
    permission: Permission? = null,
    permissions: List<Permission> = emptyList(),
    anyPermission: List<Permission> = emptyList(),
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    val hasPermission = when {
        // Single permission check
        permission != null -> 
            RoleBasedAccessControl.hasPermission(permission)
        
        // Multiple permissions - require all
        permissions.isNotEmpty() -> 
            RoleBasedAccessControl.hasAllPermissions(*permissions.toTypedArray())
        
        // Multiple permissions - any is sufficient
        anyPermission.isNotEmpty() -> 
            RoleBasedAccessControl.hasAnyPermission(*anyPermission.toTypedArray())
        
        // No permission specified - enabled
        else -> true
    }
    
    androidx.compose.material.Button(
        onClick = onClick,
        enabled = enabled && hasPermission
    ) {
        content()
    }
} 