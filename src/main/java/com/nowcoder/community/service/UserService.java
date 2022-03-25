package com.nowcoder.community.service;

import com.nowcoder.community.dao.LoginTicketMapper;
import com.nowcoder.community.dao.UserMapper;
import com.nowcoder.community.entity.LoginTicket;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;
    //当前用户从hostholder里取
    @Autowired
    private HostHolder hostHolder;

    @Value("${community.path.domain}")
    private String domain;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    public User findUserById(int id) {
        User user = getCache(id);
        if (user == null) {
            user = initCache(id);
        }
        return user;
//        return userMapper.selectById(id);
    }

    public Map<String, Object> register(User user) {
        Map<String, Object> map = new HashMap<>();

        // 空值处理
        if (user == null) {
            throw new IllegalArgumentException("参数不能为空!");
        }
        if (StringUtils.isBlank(user.getUsername())) {
            map.put("usernameMsg", "账号不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getPassword())) {
            map.put("passwordMsg", "密码不能为空!");
            return map;
        }
        if (StringUtils.isBlank(user.getEmail())) {
            map.put("emailMsg", "邮箱不能为空!");
            return map;
        }

        // 验证账号
        User u = userMapper.selectByName(user.getUsername());
        if (u != null) {
            map.put("usernameMsg", "该账号已存在!");
            return map;
        }

        // 验证邮箱
        u = userMapper.selectByEmail(user.getEmail());
        if (u != null) {
            map.put("emailMsg", "该邮箱已被注册!");
            return map;
        }

        // 注册用户
        user.setSalt(CommunityUtil.generateUUID().substring(0, 5));
        user.setPassword(CommunityUtil.md5(user.getPassword() + user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        // 激活邮件
        Context context = new Context();
        context.setVariable("email", user.getEmail());
        // http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        System.out.println("链接是:"+url);
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        mailClient.sendMail(user.getEmail(), "激活账号", content);

        return map;
    }
//激活逻辑
    public int activation(int userId, String code) {
        User user = userMapper.selectById(userId);
        if (user.getStatus() == 1) {
            return ACTIVATION_REPEAT;
        } else if (user.getActivationCode().equals(code)) {
            userMapper.updateStatus(userId, 1);
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        } else {
            return ACTIVATION_FAILURE;
        }
    }
    public Map<String,Object>login(String username,String password,int expiredSeconds){
        Map<String,Object>map =new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        //验证账号
        User user =userMapper.selectByName(username);
        if(user==null){
            map.put("usernameMsg","该账号不存在");
            return map;
        }
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活");
            return map;
        }
        //验证密码
        password=CommunityUtil.md5(password+user.getSalt());
        if(user.getPassword().equals(password)){
            map.put("passwordMsg","密码不正确");
            return map;
        }
        //生成登录凭证
        LoginTicket loginTicket=new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+expiredSeconds*1000));

//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey= RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());
        return map;
    }
    public void logout(String ticket) {

//        loginTicketMapper.updateStatus(ticket, 1);
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket=(LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);

    }


    public LoginTicket findLoginTicket(String ticket) {

//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey=RedisKeyUtil.getTicketKey(ticket);
        return (LoginTicket)redisTemplate.opsForValue().get(redisKey);
    }
    //更新头像
    public int updateHeader(int userId, String headerUrl) {

//        return userMapper.updateHeader(userId, headerUrl);
        int rows=userMapper.updateHeader(userId, headerUrl);
        clearCache(userId);
        return rows;
    }

    /**
     * 以下是我写的烂代码，注意一点啊
     * @param oldPassword
     * @param newPassword
     * @param newPassword2
     * @return
     */
//    修改密码

    public Map<String,Object>updatePassword(String oldPassword,String newPassword,String newPassword2){
        Map<String,Object>map =new HashMap<>();
        //空值处理
        if(StringUtils.isBlank(oldPassword)){
            map.put("oldPasswordMsg","旧密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPassword)){
            map.put("newPasswordMsg","新密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(newPassword2)){
            map.put("newPassword2Msg","确认密码不能为空！");
            return map;
        }
        //验证旧密码,先加密再比较
        User user =hostHolder.getUser();
        //输入的旧密码加密---》oldpswd
        String oldpswd=CommunityUtil.md5(oldPassword+user.getSalt());
        //输入的新密码加密---》newpswd
        String newpswd=CommunityUtil.md5(newPassword+user.getSalt());
        if(!user.getPassword().equals(oldpswd)){
            map.put("oldPasswordMsg","旧密码不正确");
            System.out.println("真正的旧密码为："+user.getPassword());
            System.out.println("我输入的旧密码为："+oldpswd);
            return map;
        }
        //验证新密码
        // 0e0c4ae4c3dda81868d9e350bdc40656
        if(user.getPassword().equals(newpswd)){
            map.put("newPasswordMsg","新密码不能与旧密码相同！");
            System.out.println("新密码为："+newpswd);
            System.out.println("旧密码为："+oldpswd);
            return map;
        }
        if(!newPassword.equals(newPassword2)){//注意这里不能用等号判断，等号判断的是地址，永远不一样
            map.put("newPassword2Msg","新密码两次输入不一致！");
            System.out.println("新密码两次输入不一致！");
            System.out.println("第一次为："+newPassword);
            System.out.println(newPassword);
            System.out.println("第二次为："+newPassword2);
            System.out.println(newPassword2);
            return map;
        }
        userMapper.updatePassword(user.getId(),newpswd );
        clearCache(user.getId());
        map.put("succeed","hahahah成功了好开心");
        System.out.println("hahahah成功了好开心");
        return map;

    }
    public User findUserByName(String username) {
        return userMapper.selectByName(username);
    }
    // 1.优先从缓存中取值
    private User getCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2.取不到时初始化缓存数据
    private User initCache(int userId) {
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);
        return user;
    }

    // 3.数据变更时清除缓存数据
    private void clearCache(int userId) {
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }
    //获得用户权限
    public Collection<? extends GrantedAuthority> getAuthorities(int userId) {
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {

            @Override
            public String getAuthority() {
                switch (user.getType()) {
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });
        return list;
    }

}