package org.drools.guvnor.server.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.RepositoryException;

import org.drools.repository.RulesRepository;
import org.drools.repository.security.PermissionManager;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.security.Identity;

@Name("org.drools.guvnor.server.security.RoleBasedPermissionStore")
@AutoCreate
public class RoleBasedPermissionStore {
	private static List<RoleBasedPermission> rbps = new ArrayList<RoleBasedPermission>();
	@In
	public RulesRepository repository;

	public RoleBasedPermissionStore() {
	}
	
	public List<RoleBasedPermission> getRoleBasedPermissions() {
		return null;
	}
	
	public List<RoleBasedPermission> getRoleBasedPermissionsByUserName(String userName) {
		PermissionManager permissionManager = new PermissionManager(repository);
        List<RoleBasedPermission> permissions = new ArrayList<RoleBasedPermission>();
        try {
			Map<String, List<String>> perms = permissionManager.retrieveUserPermissions(userName);
	    	for (String roleType : perms.keySet()) {
				List<String> permissionsPerRole = perms.get(roleType);
				for(String permissionPerRole: permissionsPerRole) {
					if(permissionPerRole.startsWith("package=")) {
						String packageUuid = permissionPerRole.substring("package=".length());
						permissions.add(new RoleBasedPermission(userName, roleType, packageUuid, null));
					} else if(permissionPerRole.startsWith("category=")) {
						String categoryPath = permissionPerRole.substring("category=".length());
						permissions.add(new RoleBasedPermission(userName, roleType, null, categoryPath));
					} 
				}
			}		    	
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		
		return permissions;
	}	
	
	public List<RoleBasedPermission> getRoleBasedPermissionsByPackage(String packageName) {
		return null;
	}
	
	public void addRoleBasedPermission(String userName, RoleBasedPermission rbp) {
		PermissionManager permissionManager = new PermissionManager(repository);
		try {

			Map<String, List<String>> perms = permissionManager
					.retrieveUserPermissions(userName);
			Object permissionsPerRole = perms.get(rbp.getRole());
			if (permissionsPerRole != null) {
					if (rbp.getPackageUUID() != null) {
						((List<String>) permissionsPerRole).add("package="
								+ rbp.getPackageUUID());
					} else if (rbp.getCategoryPath() != null) {
						((List<String>) permissionsPerRole).add("category="
								+ rbp.getPackageUUID());
					}

			} else {
				List<String> perm = new ArrayList<String>();
				if (rbp.getPackageUUID() != null) {
					perm.add("package=" + rbp.getPackageUUID());
				} else if (rbp.getCategoryPath() != null) {
					perm.add("category=" + rbp.getCategoryPath());
				}
				perms.put(rbp.getRole(), perm);
			}

			permissionManager.updateUserPermissions(userName, perms);
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
	}


}
