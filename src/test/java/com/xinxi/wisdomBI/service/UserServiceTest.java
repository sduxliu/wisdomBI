package com.xinxi.wisdomBI.service;

import com.xinxi.wisdomBI.model.dto.user.UserRegisterRequest;
import com.xinxi.wisdomBI.model.entity.User;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.xinxi.wisdomBI.constant.UserConstant.SALT;

/**
 * 用户服务测试
 *
 */
@SpringBootTest
public class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void userRegister() {
        //https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/31bb7f45-2e66-460f-ac85-b9f03f4a3525.jpg
        //https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/faba58e8-16e7-499a-b7bd-c1d5409fbd2f.jpg
        // https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/65d317ec-0187-47da-840e-f8440e41daec.jpg
        //https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/960f4d67-0437-46c8-a079-cf2823b6a53c.jpg
        // https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/c416f4e3-c5d0-469e-b399-d5d6dade3ee4.jpg
        List<User> users = new ArrayList<>();
        List<String> userAvatar = getUserAvatar();
        String encryptPassword = DigestUtils.md5DigestAsHex(("xinxi"+"12345678").getBytes());
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setUserName("用户" + i);
            user.setUserAccount("user" + i);
            user.setUserAvatar(userAvatar.get(i % 5));
            user.setUserGender(i % 2);
            user.setUserPhone("1334567890" + i);
            user.setUserPassword(encryptPassword);
            users.add(user);
        }
        // 保存用户
        userService.saveBatch(users);
    }

    @NotNull
    private List<String> getUserAvatar() {
        List<String>userAvatar = new ArrayList<>();
        userAvatar.add("https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/31bb7f45-2e66-460f-ac85-b9f03f4a3525.jpg");
        userAvatar.add("https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/faba58e8-16e7-499a-b7bd-c1d5409fbd2f.jpg");
        userAvatar.add("https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/65d317ec-0187-47da-840e-f8440e41daec.jpg");
        userAvatar.add("https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/960f4d67-0437-46c8-a079-cf2823b6a53c.jpg");
        userAvatar.add("https://xinxi-imgstore.oss-cn-hangzhou.aliyuncs.com/c416f4e3-c5d0-469e-b399-d5d6dade3ee4.jpg");
        return userAvatar;
    }

}
