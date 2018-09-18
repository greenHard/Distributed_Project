package com.zhang.webservice.cxf.service;

import com.zhang.webservice.cxf.Response;
import com.zhang.webservice.cxf.User;

import javax.jws.WebService;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * webService 和spring整合
 * CXF + Rest风格
 */
@WebService
@Path(value = "/users/")
public interface UserService {

    @GET
    @Path("/") //http://ip:port/users
    @Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML})
    List<User> getUsers();

    @DELETE
    @Path("{id}") // http://ip:port/users/1
    @Consumes({MediaType.APPLICATION_JSON,MediaType.APPLICATION_XML}) //请求accept
    Response deleteUser();

    @GET
    @Path("{id}") //http://ip:port/users/1
    @Produces({MediaType.APPLICATION_JSON}) //请求accept
    User getUser();

    @POST
    @Path("add")
    Response insert(User user);

    @PUT
    @Path("update")
    Response update(User user);



}
