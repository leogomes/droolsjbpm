package org.drools.guvnor.server.security;

import static org.jboss.seam.ScopeType.APPLICATION;
import static org.jboss.seam.annotations.Install.FRAMEWORK;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.drools.guvnor.server.ServiceImplementation;
import org.drools.repository.PackageItem;
import org.drools.repository.RulesRepository;
import org.drools.repository.RulesRepositoryException;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.log.LogProvider;
import org.jboss.seam.log.Logging;
import org.jboss.seam.security.Identity;
import org.jboss.seam.security.Role;
import org.jboss.seam.security.SimplePrincipal;
import org.jboss.seam.security.permission.PermissionResolver;

/**
 * Resolves package-based permissions. A user might have differnt permissions on different packages.
 *  
 * @author Jervis Liu
 */
@Name("org.drools.guvnor.server.security.packageBasedPermissionResolver")
@Scope(APPLICATION)
@BypassInterceptors
@Install(precedence = org.jboss.seam.annotations.Install.APPLICATION)
@Startup
public class PackageBasedPermissionResolver implements PermissionResolver,
		Serializable {

	@Create
	public void create() {
	}

	public boolean hasPermission(Object target, String action) {
		List<RoleBasedPermission> permissions = (List<RoleBasedPermission>) Contexts
				.getSessionContext().get("packageBasedPermission");

		String targetUUDI = "";
		
		if (target instanceof PackageUUIDType) {
			targetUUDI = ((PackageUUIDType) target).getUUID();
		} else if (target instanceof PackageNameType) {
			try {
				ServiceImplementation si = (ServiceImplementation) Component
						.getInstance("org.drools.guvnor.client.rpc.RepositoryService");
				PackageItem source = si.repository
						.loadPackage(((PackageNameType) target)
								.getPackageName());
				targetUUDI = source.getUUID();
			} catch (RulesRepositoryException e) {
				return false;
			}

		} else {
			// PackageBasedPermissionResolver only grants permissions based on package info. 
			// Return false if the input is not a pacakge info, as this will be the reponsibility 
			//of other PermissionResolvers in the resolver chain.
			return false;
		}
		
		//admin can do everything
		if (Identity.instance().hasRole(RoleTypes.ADMIN)) {
			return true;
		}
		
		for (RoleBasedPermission pbp : permissions) {
			//only when the user has the permission to perform the specific action on this package
			if (targetUUDI.equalsIgnoreCase(pbp.getPackageUUID())
					&& isPermitted(action, pbp.getRole())) {
				return true;
			}
		}

		return false;
	}

	private boolean isPermitted(String requestedAction, String role) {
		if (RoleTypes.PACKAGE_ADMIN.equalsIgnoreCase(role)) {
			return true;
		} else if (RoleTypes.PACKAGE_DEVELOPER.equalsIgnoreCase(role)) {
			if ("package.admin".equalsIgnoreCase(requestedAction)) {
				return false;
			} else if ("package.developer".equalsIgnoreCase(requestedAction)) {
				return true;
			} else if ("package.readonly".equalsIgnoreCase(requestedAction)) {
				return true;
			}
		} else if (RoleTypes.PACKAGE_READONLY.equalsIgnoreCase(role)) {
			if ("package.admin".equalsIgnoreCase(requestedAction)) {
				return false;
			} else if ("package.developer".equalsIgnoreCase(requestedAction)) {
				return false;
			} else if ("package.readonly".equalsIgnoreCase(requestedAction)) {
				return true;
			}
		}

		return false;
	}

	public void filterSetByAction(Set<Object> targets, String action) {
	}
}
