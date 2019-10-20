
## Keycloak

**Default groups**
* Group name: `Users`, roles: `[ROLE_USER]`
* Group name: `Admins`, roles: `[ROLE_ADMIN]`

**Default users**
* Username: `user`, Password: `user`, Groups: `[Users]`
* Username: `admin`, Password: `admin`, Groups: `[Admin]`

_Tips_
Extract Keycloack users
```
/opt/jboss/keycloak/bin/standalone.sh -Dkeycloak.migration.action=export -Dkeycloak.migration.usersExportStrategy=DIFFERENT_FILES -Dkeycloak.migration.realmName=todo-app -Dkeycloak.migration.provider=dir -Dkeycloak.migration.dir=/tmp
```