package com.qianlima.offline.controller;

import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

@RestController
@RequestMapping("/f")
@Api("free")
@Slf4j
public class FreemarkerController {
    @GetMapping(value = "/my")
    public ModelAndView my(ModelMap modelMap){

        ModelAndView mv = new ModelAndView("test");
        //modelMap.addAttribute("name","pengxingjiang");
        mv.addObject("address","四川-宜宾");
        HashMap<String,String> userInfo = new HashMap<>();
        userInfo.put("name","111");
        userInfo.put("tel","18888888888");
        mv.addObject("userInfo",userInfo);
        return mv;
    }
}