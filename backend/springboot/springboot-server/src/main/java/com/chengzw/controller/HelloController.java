package com.chengzw.controller;

import com.chengzw.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengzw
 * @description
 * @since 2021/7/3
 */

@RestController
public class HelloController {

    @Autowired
    HelloService helloService;

    // 后端解决跨域问题方式三，使用该注解就注释 CrosConfig 和 WebMvcConfig，@CrossOrigin(origins = "*",allowCredentials="true",allowedHeaders = "*",methods = {})
    @CrossOrigin
    @RequestMapping("/api/data")
    public String hello(){
        return helloService.hello();
    }
}
