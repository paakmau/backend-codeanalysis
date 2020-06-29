package local.happysixplus.backendcodeanalysis.controller;

import org.springframework.web.bind.annotation.RestController;

import local.happysixplus.backendcodeanalysis.service.MessageService;
import local.happysixplus.backendcodeanalysis.vo.UserVo;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping(value = "/contact")
public class ContactController {
    @Autowired
    MessageService service;

    // 获取所有联系人
    @GetMapping(value = "/{id}")
    public List<UserVo> getContacts(@PathVariable Long id) {
        return service.getContacts(id);
    }

}