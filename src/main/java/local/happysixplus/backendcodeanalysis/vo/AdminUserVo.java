package local.happysixplus.backendcodeanalysis.vo;

import lombok.Data;

@Data
public class AdminUserVo{
    Long id;
    String username;
    String pwdMd5;
    String inviteCode;
}