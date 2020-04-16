package org.flowable.ui.idm.rest.app;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.flowable.idm.api.Group;
import org.flowable.idm.api.User;
import org.flowable.ui.common.model.GroupRepresentation;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.service.exception.NotFoundException;
import org.flowable.ui.idm.model.UserInformation;
import org.flowable.ui.idm.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yangjf
 * @version 0.1.0
 * @time 2020/4/16
 */
@RestController("apiUsersResource")
@RequestMapping("/api")
public class ApiUsersResource {
    @Autowired
    protected UserService userService;

    public ApiUsersResource() {
    }

    @RequestMapping(
            value = {"/idm/users/{userId}"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public UserRepresentation getUserInformation(@PathVariable String userId) {
        UserInformation userInformation = this.userService.getUserInformation(userId);
        if(userInformation == null) {
            throw new NotFoundException();
        } else {
            UserRepresentation userRepresentation = new UserRepresentation(userInformation.getUser());
            Iterator var4;
            if(userInformation.getGroups() != null) {
                var4 = userInformation.getGroups().iterator();

                while(var4.hasNext()) {
                    Group group = (Group)var4.next();
                    userRepresentation.getGroups().add(new GroupRepresentation(group));
                }
            }

            if(userInformation.getPrivileges() != null) {
                var4 = userInformation.getPrivileges().iterator();

                while(var4.hasNext()) {
                    String privilege = (String)var4.next();
                    userRepresentation.getPrivileges().add(privilege);
                }
            }

            return userRepresentation;
        }
    }

    @RequestMapping(
            value = {"/idm/users"},
            method = {RequestMethod.GET},
            produces = {"application/json"}
    )
    public List<UserRepresentation> findUsersByFilter(@RequestParam("filter") String filter) {
        List<User> users = this.userService.getUsers(filter, (String)null, (Integer)null);
        List<UserRepresentation> result = new ArrayList();
        Iterator var4 = users.iterator();

        while(var4.hasNext()) {
            User user = (User)var4.next();
            result.add(new UserRepresentation(user));
        }

        return result;
    }
}

