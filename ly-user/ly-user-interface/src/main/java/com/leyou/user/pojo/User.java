package com.leyou.user.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Data
@Table(name = "tb_user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 用户名
    @NotBlank(message = "用户名不能为空")
    @Length(min = 4, max = 32, message = "密码长度必须在4~32位")
    private String username;
    // 密码
    @Length(min = 4, max = 32, message = "密码长度必须在4~32位")
    @JsonIgnore
    private String password;
    // 电话
    @Pattern(regexp = "^[1](([3][0-9])|([4][5-9])|([5][0-3,5-9])|([6][5,6])|([7][0-8])|([8][0-9])|([9][1,8,9]))[0-9]{8}$", message = "手机号格式不正确")
    private String phone;
    // 创建时间
    private Date created;
    // 密码的盐值
    @JsonIgnore
    private String salt;

}