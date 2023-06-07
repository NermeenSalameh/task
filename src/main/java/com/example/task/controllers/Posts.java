package com.example.task.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class Posts {
    @GetMapping(path = "/api/posts")
    public String getHostName(HttpServletRequest request) {
        return getBaseUrl(request) + "/api/posts";
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = request.getHeader("Host");
        return scheme + "://" + host;
    }
}
